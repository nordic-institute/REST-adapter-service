/*
 * Copyright 2021 the original author or authors.
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

package org.gradle.internal.jvm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * These JVM arguments should be passed to any Gradle process that will be running on Java 9+
 * Gradle accesses those packages reflectively. On Java versions 9 to 15, the users will get
 * a warning they can do nothing about. On Java 16+, strong encapsulation of JDK internals is
 * enforced and not having the explicit permissions for reflective accesses will result in runtime exceptions.
 */
public class JpmsConfiguration {

    public static final List<String> GROOVY_JPMS_ARGS = Collections.unmodifiableList(Arrays.asList(
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED",
        "--add-opens=java.prefs/java.util.prefs=ALL-UNNAMED", // required by PreferenceCleaningGroovySystemLoader
        "--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED", // Required by JdkTools and JdkJavaCompiler
        "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED" // Required by JdkTools and JdkJavaCompiler
    ));

    public static final List<String> GRADLE_DAEMON_JPMS_ARGS;

    static {
        List<String> gradleDaemonJvmArgs = new ArrayList<String>(GROOVY_JPMS_ARGS);

        List<String> configurationCacheJpmsArgs = Collections.unmodifiableList(Arrays.asList(
            "--add-opens=java.base/java.util=ALL-UNNAMED", // for overriding environment variables
            "--add-opens=java.prefs/java.util.prefs=ALL-UNNAMED", // required by JavaObjectSerializationCodec.kt
            "--add-opens=java.base/java.nio.charset=ALL-UNNAMED", // required by BeanSchemaKt
            "--add-opens=java.base/java.net=ALL-UNNAMED", // required by JavaObjectSerializationCodec
            "--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED", // serialized from org.gradle.internal.file.StatStatistics$Collector
            "--add-opens=java.xml/javax.xml.namespace=ALL-UNNAMED" // serialized from IvyDescriptorFileGenerator.Model
        ));
        gradleDaemonJvmArgs.addAll(configurationCacheJpmsArgs);

        GRADLE_DAEMON_JPMS_ARGS = Collections.unmodifiableList(gradleDaemonJvmArgs);
    }
}
