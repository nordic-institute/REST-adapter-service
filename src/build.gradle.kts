import org.apache.tools.ant.filters.ReplaceTokens
import java.util.*

plugins {
    java
    `maven-publish`
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.owasp.dependencycheck") version "12.1.1"
//    `checkstyle`
    jacoco
}


//sourceSets {
//    create("integrationTest") {
//        java.srcDirs("src/test/java/")
//        resources.srcDir("src/test/resources")
//        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output + configurations["testRuntimeClasspath"]
//        runtimeClasspath += output + compileClasspath
//        configurations {
//            named("integrationTestImplementation") {
//                extendsFrom(configurations["testImplementation"])
//            }
//            named("integrationTestRuntimeOnly") {
//                extendsFrom(configurations["testRuntimeOnly"])
//            }
//        }
//    }
//
//}

repositories {
    mavenLocal()
    maven {
        url = uri("https://artifactory.niis.org/xroad-maven-releases")
        mavenContent {
            releasesOnly()
        }
    }
    maven {
        url = uri("https://artifactory.niis.org/xroad-maven-snapshots")
        mavenContent {
            snapshotsOnly()
        }
    }
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
    mavenCentral()
}



ext {
    set("xrd4j.version", "0.6.0")
    set("java.version", "21")
    set("jdk.version", "21")
//    set("xmlunit.version", "2.7.0")
//    set("failsafe.version", "2.19.1")
    set("project.build.sourceEncoding", "UTF-8")
    set("project.build.resourceEncoding", "UTF-8")
    set("project.reporting.outputEncoding", "UTF-8")
//    set("tomcat.version", "10.0.39")
    set("app.home", "/var/lib/tomcat/webapps")
    // set("sonar.junit.reportPaths", "target/failsafe-reports,target/surefire-reports")
    set("server.port", "9898")
}

dependencies {
    // SpringBoot
    implementation("org.springframework.boot:spring-boot-starter-web:3.4.4")
    implementation("org.springframework.boot:spring-boot-starter-aop:3.4.4")
    implementation("org.springframework.boot:spring-boot-starter-tomcat:3.4.4")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.4.4") {
        exclude(group = "com.vaadin.external.google", module = "android-json")
    }

    // xrd4j
    implementation("org.niis.xrd4j:common:0.6.0")
    implementation("org.niis.xrd4j:client:0.6.0")
    implementation("org.niis.xrd4j:server:0.6.0")
    implementation("org.niis.xrd4j:rest:0.6.0")

    implementation("jakarta.servlet:jakarta.servlet-api:6.0.0")
    implementation("jakarta.xml.soap:jakarta.xml.soap-api:3.0.2")
    implementation("com.sun.xml.messaging.saaj:saaj-impl:3.0.4")
    implementation("org.apache.httpcomponents.client5:httpclient5:5.4.1")


    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    testImplementation("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

    //Test Implementation
    testImplementation(libs.org.xmlunit.xmlunit.assertj)
    testImplementation("org.apache.tomcat.embed:tomcat-embed-jasper:10.1.39")

    testImplementation("org.wiremock.integrations:wiremock-spring-boot:3.6.0")
    testAnnotationProcessor("org.wiremock.integrations:wiremock-spring-boot:3.6.0")

    testImplementation(libs.commons.io.commons.io)
    testImplementation(libs.com.github.stefanbirkner.system.rules)

    testImplementation(libs.org.xmlunit.xmlunit.core)
    testImplementation(libs.org.xmlunit.xmlunit.matchers)
    testImplementation(libs.com.jayway.jsonpath.json.path.assert)
    testImplementation(libs.com.jayway.jsonpath.json.path)

}



group = "org.niis"
version = "1.1.0-SNAPSHOT"
description = "REST Adapter Service"
java.sourceCompatibility = JavaVersion.VERSION_21

publishing {
    publications {
        create<MavenPublication>("rest-adaper-service-extension") {
            from(components["java"])

            pom {
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("http://www.opensource.org/licenses/mit-license.php")
                    }
                }
                scm {
                    connection.set("scm:git:git@github.com:nordic-institute/REST-adapter-service.git")
                    developerConnection.set("scm:git:git@github.com:nordic-institute/REST-adapter-service.git")
                    url.set("https://github.com/nordic-institute/REST-adapter-service.git")
                }
                developers {
                    developer {
                        id.set("niis")
                        name.set("Nordic Institute for Interoperability Solutions (NIIS)")
                        roles.set(listOf("architect", "developer"))
                        timezone.set("+2")
                    }
                    developer {
                        id.set("vrk")
                        name.set("Population Register Centre (VRK)")
                        roles.set(listOf("architect", "developer"))
                        timezone.set("+2")
                    }
                    developer {
                        id.set("petkivim")
                        name.set("Petteri Kivimäki")
                        roles.set(listOf("architect", "developer"))
                        timezone.set("+2")
                    }
                }
            }
        }
    }
}

dependencyCheck {
//    formats = ("xml", "json")
    outputDirectory = "${project.projectDir}/build/reports/dependency-check-report"
    suppressionFile = "${project.projectDir}/src/dependency-check-suppressions.xml"
}
tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
    options.annotationProcessorPath = configurations.annotationProcessor.get()
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}

val isEncrypted = project.hasProperty("encrypted")
val adapterProfileDir = if (isEncrypted) "encrypted" else "plaintext"

val filterFile = file("src/main/filters/default.properties")
val props = Properties().apply {
    load(filterFile.reader())
}
val replacements = props.entries.associate { it.key.toString() to it.value.toString() }

tasks.processResources {
    filesMatching("**/*.properties") {
        filter { line ->
            line.replace("@projectDir@", project.projectDir.toString())
        }
    }
    from("src/main/profiles/$adapterProfileDir/") {
        filesMatching("**/*.properties") {
            filter<ReplaceTokens>("tokens" to replacements)
        }
    }
    filesMatching("src/main/resources") {
        expand(project.properties)

        val props = Properties().apply {
            load(filterFile.reader())
        }
        expand(props.entries.associate { it.key.toString() to it.value })
    }
}

tasks.processTestResources {
    filesMatching("**/*.properties") {
        filter { line ->
            line.replace("@projectDir@", project.projectDir.toString())
        }
    }

}

tasks.test {
    useJUnitPlatform()
    exclude("org/niis/xroad/restadapterservice/ConsumerGatewayIT.class")
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}

tasks.register("processIntegrationTestResources") {
//    group = "build"
    description = "Processes integration test resources"
    dependsOn("processResources")
    doLast {
        copy {
            from("src/main/profiles/$adapterProfileDir/") {
                filesMatching("**/*.properties") {
                    filter<ReplaceTokens>("tokens" to replacements)
                }
            }
            into("${project.projectDir}/build/resources/integration-test-profile")
        }
    }
}

tasks.register<Test>("iTest") {
    useJUnitPlatform()
    dependsOn("processTestResources")
    description = "Runs integration tests"
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath

    include("org/niis/xroad/restadapterservice/ConsumerGatewayIT.class")

    // System properties — like Maven <systemProperties>
    systemProperty("log4j.configuration", "test-log4j.xml")
    systemProperty("consumerPath", project.projectDir.resolve("libs").resolve("${project.name}-${project.version}.jar"))
    systemProperty("server.port", "9898")
    systemProperty("propertiesDirectory", "${project.projectDir}/build/resources/integration-test-profile")

}

tasks.bootJar {
    archiveBaseName.set("rest-adapter-service")
    manifest {
        attributes["main-class"] = "org.niis.xroad.restadapterservice.Application"
    }
    // mainClass.set("org.niis.xroad.restadapterservice.Application")

    // archiveBaseName.set("my-kotlin-app")
    // archiveVersion.set(project.version.toString())

    // // Include compiled classes and resources
    // from(sourceSets.main.get().output)
}
