/*
 * Copyright 2009 the original author or authors.
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
package org.gradle.api.artifacts;

import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskDependency;

import java.io.File;
import java.util.Set;

/**
 * A {@code FileCollectionDependency} is a {@link Dependency} on a collection of local files which are not stored in a
 * repository.
 */
@SuppressWarnings("deprecation") // Because of SelfResolvingDependency
public interface FileCollectionDependency extends SelfResolvingDependency {
    /**
     * Returns the files attached to this dependency.
     *
     * @since 3.3
     */
    FileCollection getFiles();

    /**
     * {@inheritDoc}
     *
     * @deprecated This class will no longer implement {@link SelfResolvingDependency} in Gradle 9.0.
     */
    @Override
    @Deprecated
    TaskDependency getBuildDependencies();

    /**
     * {@inheritDoc}
     *
     * @deprecated This class will no longer implement {@link SelfResolvingDependency} in Gradle 9.0. Use {@link #getFiles()} instead.
     */
    @Override
    @Deprecated
    Set<File> resolve();

    /**
     * {@inheritDoc}
     *
     * @deprecated This class will no longer implement {@link SelfResolvingDependency} in Gradle 9.0. Use {@link #getFiles()} instead.
     */
    @Override
    @Deprecated
    Set<File> resolve(boolean transitive);
}
