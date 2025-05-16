/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.attributes;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.internal.provider.ProviderInternal;
import org.gradle.api.provider.Provider;
import org.gradle.internal.Cast;
import org.gradle.internal.deprecation.DeprecationLogger;
import org.gradle.internal.isolation.Isolatable;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

final class DefaultMutableAttributeContainer extends AbstractAttributeContainer implements AttributeContainerInternal {
    private final Map<Attribute<?>, Isolatable<?>> attributes = new LinkedHashMap<>(); // Need to maintain insertion order here, this is indirectly tested
    private Map<Attribute<?>, Provider<?>> lazyAttributes = Cast.uncheckedCast(Collections.EMPTY_MAP);
    private boolean realizingAttributes = false;

    private final AttributesFactory attributesFactory;
    private final AttributeValueIsolator attributeValueIsolator;

    private ImmutableAttributes immutableValue;

    public DefaultMutableAttributeContainer(AttributesFactory attributesFactory, AttributeValueIsolator attributeValueIsolator) {
        this.attributesFactory = attributesFactory;
        this.attributeValueIsolator = attributeValueIsolator;
    }

    @Override
    public String toString() {
        maybeEmitRecursiveQueryDeprecation();
        final Map<Attribute<?>, Object> sorted = new TreeMap<>(Comparator.comparing(Attribute::getName));
        lazyAttributes.keySet().forEach(key -> sorted.put(key, lazyAttributes.get(key).toString()));
        attributes.keySet().forEach(key -> sorted.put(key, attributes.get(key).toString()));
        return sorted.toString();
    }

    @Override
    public Set<Attribute<?>> keySet() {
        maybeEmitRecursiveQueryDeprecation();
        // Need to copy the result since if the user calls getAttribute() while iterating over the returned set,
        // realizing a lazy attribute will add to the eager `attributes` map and remove from the `lazyAttributes`.
        // This avoids a ConcurrentModificationException.
        return ImmutableSet.copyOf(Sets.union(attributes.keySet(), lazyAttributes.keySet()));
    }

    @Override
    public <T> AttributeContainer attribute(Attribute<T> key, T value) {
        checkInsertionAllowed(key);
        doInsertion(key, value);
        return this;
    }

    private <T> void doInsertion(Attribute<T> key, T value) {
        assertAttributeValueIsNotNull(value);
        assertAttributeTypeIsValid(value.getClass(), key);
        immutableValue = null;
        attributes.put(key, attributeValueIsolator.isolate(value));
        removeLazyAttributeIfPresent(key);
    }

    private <T> void removeLazyAttributeIfPresent(Attribute<T> key) {
        lazyAttributes.remove(key);
    }

    @Override
    public <T> AttributeContainer attributeProvider(Attribute<T> key, Provider<? extends T> provider) {
        checkInsertionAllowed(key);
        assertAttributeValueIsNotNull(provider);
        // We can only sometimes check the type of the provider ahead of time.
        // When realizing this provider and inserting its value into the container, we still
        // check the value type is appropriate. see doInsertion
        if (provider instanceof ProviderInternal) {
            Class<T> valueType = Cast.<ProviderInternal<T>>uncheckedCast(provider).getType();
            if (valueType != null) {
                assertAttributeTypeIsValid(valueType, key);
            }
        }
        doInsertionLazy(key, provider);
        return this;
    }

    private <T> void checkInsertionAllowed(Attribute<T> key) {
        if (realizingAttributes) {
            throw new IllegalStateException("Cannot add new attribute '" + key.getName() + "' while realizing all attributes of the container.");
        }
        for (Attribute<?> attribute : keySet()) {
            String name = key.getName();
            if (attribute.getName().equals(name) && attribute.getType() != key.getType()) {
                throw new IllegalArgumentException("Cannot have two attributes with the same name but different types. "
                    + "This container already has an attribute named '" + name + "' of type '" + attribute.getType().getName()
                    + "' and you are trying to store another one of type '" + key.getType().getName() + "'");
            }
        }
    }

    /**
     * Checks that the attribute's type matches the given value's type is the expected value type.
     *
     * @param valueType the value type to check
     * @param attribute the attribute containing a type to check against
     */
    private <T> void assertAttributeTypeIsValid(Class<?> valueType, Attribute<T> attribute) {
        if (!attribute.getType().isAssignableFrom(valueType)) {
            throw new IllegalArgumentException(String.format("Unexpected type for attribute '%s' provided. Expected a value of type %s but found a value of type %s.", attribute.getName(), attribute.getType().getName(), valueType.getName()));
        }
    }

    private void assertAttributeValueIsNotNull(@Nullable Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Setting null as an attribute value is not allowed");
        }
    }

    @Override
    public <T> T getAttribute(Attribute<T> key) {
        maybeEmitRecursiveQueryDeprecation();
        Isolatable<?> value = attributes.get(key);
        if (value == null) {
            if (lazyAttributes.containsKey(key)) {
                return realizeLazyAttribute(key);
            } else {
                return null;
            }
        } else {
            return Cast.uncheckedCast(value.isolate());
        }
    }

    @Override
    public ImmutableAttributes asImmutable() {
        maybeEmitRecursiveQueryDeprecation();
        realizeAllLazyAttributes();
        if (immutableValue == null) {
            immutableValue = attributesFactory.fromMap(attributes);
        }
        return immutableValue;
    }

    private void maybeEmitRecursiveQueryDeprecation() {
        if (realizingAttributes) {
            DeprecationLogger.deprecateBehaviour("Querying the contents of an attribute container while realizing attributes of the container.")
                .willBecomeAnErrorInGradle9()
                .withUpgradeGuideSection(8, "attribute_container_recursive_query")
                .nagUser();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultMutableAttributeContainer that = (DefaultMutableAttributeContainer) o;

        return Objects.equals(asImmutable(), that.asImmutable());
    }

    @Override
    public int hashCode() {
        return asImmutable().hashCode();
    }

    private <T> void doInsertionLazy(Attribute<T> key, Provider<? extends T> provider) {
        if (lazyAttributes == Collections.EMPTY_MAP) {
            lazyAttributes = new LinkedHashMap<>(1);
        }
        lazyAttributes.put(key, provider);
        removeAttributeIfPresent(key);
    }

    private <T> void removeAttributeIfPresent(Attribute<T> key) {
        immutableValue = null;
        attributes.remove(key);
    }

    private <T> T realizeLazyAttribute(Attribute<T> key) {
        @SuppressWarnings("unchecked") final T value = (T) lazyAttributes.get(key).get();
        doInsertion(key, value);
        return value;
    }

    private void realizeAllLazyAttributes() {
        if (!lazyAttributes.isEmpty()) {
            // As doInsertion will remove an item from lazyAttributes, we can't iterate that collection directly here, or else we'll get ConcurrentModificationException
            final Set<Attribute<?>> savedKeys = new LinkedHashSet<>(lazyAttributes.keySet());
            try {
                realizingAttributes = true;
                savedKeys.forEach(key -> {
                    Provider<?> value = lazyAttributes.get(key);
                    // Between getting the list of keys and realizing the values
                    // some lazy attributes have been realized and removed from the map
                    // This can happen when a side effect of calculating the value of a Provider
                    // causes dependency resolution or evaluation of the attributes of
                    // the same AttributeContainer
                    if (value != null) {
                        doInsertion(Cast.uncheckedNonnullCast(key), value.get());
                    }
                });
            } finally {
                realizingAttributes = false;
            }
        }
    }
}
