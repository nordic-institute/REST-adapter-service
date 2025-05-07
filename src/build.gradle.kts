import org.apache.tools.ant.filters.ReplaceTokens
import java.util.*

plugins {
    java
    checkstyle
    `maven-publish`
    alias(libs.plugins.springBoot)
    alias(libs.plugins.dependencyManagement)
    id("org.owasp.dependencycheck") version ("12.1.1")
    jacoco
}

// Profile
val isEncrypted = project.property("encrypted") == "true"
val adapterProfileDir = if (isEncrypted) "encrypted" else "plaintext"


checkstyle {
    toolVersion = "10.23.1"
    group = "verification"
    configFile = file("${project.projectDir}/config/checkstyle/checkstyle-config.xml")
}

dependencyCheck {

    outputDirectory = "${project.projectDir}/build/reports/dependency-check-report"
    suppressionFile = "${project.projectDir}/src/dependency-check-suppressions.xml"
    formats = listOf("HTML", "XML")
    nvd.validForHours = 24
    skipConfigurations = listOf("checkstyle")

//    if (project.hasProperty("nvdApiKey")) {
//        nvd.apiKey = project.property("nvdApiKey") as String
//    }
}

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
    set("java.version", "21")
    set("jdk.version", "21")
    set("project.build.sourceEncoding", "UTF-8")
    set("project.build.resourceEncoding", "UTF-8")
    set("project.reporting.outputEncoding", "UTF-8")
    set("app.home", "/var/lib/tomcat/webapps")
    set("server.port", "9898")
}

dependencies {
    // SpringBoot
    implementation(libs.org.springframework.boot.spring.boot.starter.web)
    implementation(libs.org.springframework.boot.spring.boot.starter.aop)
    implementation(libs.org.springframework.boot.spring.boot.starter.tomcat)
    testImplementation(libs.org.springframework.boot.spring.boot.starter.test) {
        exclude(group = "com.vaadin.external.google", module = "android-json")
    }

    // xrd4j
    implementation(libs.org.niis.xrd4j.common)
    implementation(libs.org.niis.xrd4j.client)
    implementation(libs.org.niis.xrd4j.server)
    implementation(libs.org.niis.xrd4j.rest)

    // Lombok
    compileOnly(libs.org.projectlombok.lombok)
    annotationProcessor(libs.org.projectlombok.lombok)
    testImplementation(libs.org.projectlombok.lombok)
    testAnnotationProcessor(libs.org.projectlombok.lombok)

    // other
    implementation("jakarta.servlet:jakarta.servlet-api:6.0.0")
    implementation("jakarta.xml.soap:jakarta.xml.soap-api:3.0.2")
    implementation("com.sun.xml.messaging.saaj:saaj-impl:3.0.4")
    implementation("org.apache.httpcomponents.client5:httpclient5:5.4.1")
    implementation("org.apache.tomcat.embed:tomcat-embed-jasper:10.1.39")
    checkstyle("com.puppycrawl.tools:checkstyle:10.23.1")

    //Test Implementation
    testImplementation(libs.org.xmlunit.xmlunit.assertj)
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


tasks.processResources {
    val filterFile = file("src/main/filters/default.properties")
    val props = Properties().apply {
        load(filterFile.reader())
    }
    val replacements = props.entries.associate { it.key.toString() to it.value.toString() }

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
        expand(props.entries.associate { it.key.toString() to it.value })
    }
    if (isEncrypted) {
        copy {
            from("src/main/resources-bin/") {
                include("**/*.jks")
            }
            into("build/resources/main/resources-bin")
        }
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

tasks.register("processIntTestResources") {
    group = "test"
    description = "Processes integration test resources"

    val filterFile = file("src/main/filters/default.properties")
    val filterFile2 = file("src/main/filters/integration-test.properties")
    val props = Properties().apply {
        load(filterFile.reader())
        load(filterFile2.reader())
    }
    val replacements = props.entries.associate { it.key.toString() to it.value.toString() }
    doLast {
        copy {
            from("src/main/profiles/$adapterProfileDir/") {
                filesMatching("**/*.properties") {
                    filter<ReplaceTokens>("tokens" to replacements)
                    filter { line ->
                        line.replace("@projectDir@", project.projectDir.toString())
                    }
                }
            }
            into("${project.projectDir}/build/resources/integration-test-profile")
        }
    }
}

tasks.register<Test>("intTest") {
    useJUnitPlatform()
    dependsOn("processIntTestResources")
    description = "Runs integration tests"
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath

    include("org/niis/xroad/restadapterservice/ConsumerGatewayIT.class")

    systemProperty("log4j.configuration", "test-log4j.xml")
    systemProperty("consumerPath", project.projectDir.resolve("libs").resolve("${project.name}-${project.version}.jar"))
    systemProperty("server.port", "9898")
    systemProperty("propertiesDirectory", "${project.projectDir}/build/resources/integration-test-profile")
}



