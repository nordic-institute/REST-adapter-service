import org.apache.tools.ant.filters.ReplaceTokens
import java.util.*

plugins {
    java
    checkstyle
    alias(libs.plugins.springBoot)
    alias(libs.plugins.dependencyManagement)
    alias(libs.plugins.licenseGradlePlugin)
    jacoco
}

group = "org.niis"
version = "1.1.0-SNAPSHOT"
description = "REST Adapter Service"
java.sourceCompatibility = JavaVersion.VERSION_21

checkstyle {
    toolVersion = "10.23.1"
    group = "verification"
    configFile = file("${project.projectDir}/config/checkstyle/checkstyle-config.xml")
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
    implementation(libs.org.springframework.boot.spring.boot.starter.thymeleaf)
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
    implementation(libs.org.apache.tomcat.embed.tomcat.embed.jasper)
    checkstyle(libs.com.puppycrawl.tools.checkstyle)

    //Test Implementation
    testImplementation(libs.com.github.stefanbirkner.system.rules)
    testImplementation(libs.com.jayway.jsonpath.json.path)
    testImplementation(libs.com.jayway.jsonpath.json.path.assert)
    testImplementation(libs.org.xmlunit.xmlunit.assertj)
    testImplementation(libs.commons.io.commons.io)
    testImplementation(libs.org.xmlunit.xmlunit.core)
    testImplementation(libs.org.xmlunit.xmlunit.matchers)
    testImplementation(libs.org.wiremock.integrations.wiremock.spring.boot)
    testAnnotationProcessor(libs.org.wiremock.integrations.wiremock.spring.boot)
}

license {
    header = file("../LICENSE")
    mapping("java", "SLASHSTAR_STYLE")
    include("**/*.java")
}

tasks.bootRun() {
    if (project.hasProperty("customPropertiesDir")) {
        systemProperty("customPropertiesDir", project.property("customPropertiesDir") as String)
    }
}

tasks.named<ProcessResources>("processTestResources") {
    group = "verification"
    val replacements = mutableMapOf(
        "projectDir" to project.projectDir.toString()
    )

    exclude("**/application-intTest-properties/")
    exclude("**/application-intTest-keys/")

    filesMatching("**/*.properties") {
        filter<ReplaceTokens>("tokens" to replacements)
    }
}

tasks.test {
    group = "verification"
    dependsOn("processTestResources")

    useJUnitPlatform()
    exclude("org/niis/xroad/restadapterservice/ConsumerGatewayIT.class")
    finalizedBy(tasks.jacocoTestReport)
}

// disable the default jar task of the Java plugin so only springboot's jar is created
tasks.jar {
    isEnabled = false
}

tasks.register<ProcessResources>("processIntTestResources") {
    description = "Processes resources for integration tests"
    group = "verification"

    val intTestFilterFile = file("src/test/filters/integration-test.properties")
    val props = Properties().apply {
        load(intTestFilterFile.reader())
    }

    val baseReplacements = mutableMapOf(
        "projectDir" to project.projectDir.toString()
    )

    var replacements =  baseReplacements + props.entries.associate { it.key.toString() to it.value.toString() }

    var customPropertiesDir = project.findProperty("customPropertiesDir")?.toString() ?:
        "${project.projectDir}/src/test/resources/application-intTest-properties/plaintext"
    copy {
        from(customPropertiesDir) {
            logger.info("Using properties from: $customPropertiesDir")
            filesMatching("**/*.properties") {
                filter<ReplaceTokens>("tokens" to replacements)
            }
        }
        into("${project.projectDir}/build/resources/integration-test/application-intTest-properties")
    }
    copy {
        from("src/test/resources/application-intTest-keys")
        into("${project.projectDir}/build/resources/integration-test/application-intTest-keys")
    }

}

tasks.register<Test>("intTest") {
    description = "Runs integration tests"
    group = "verification"

    dependsOn("processIntTestResources")
    useJUnitPlatform()
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath

    include("org/niis/xroad/restadapterservice/ConsumerGatewayIT.class")

    systemProperty("customPropertiesDir", "${project.projectDir}/build/resources/integration-test/application-intTest-properties")
    systemProperty("consumerPath", project.projectDir.resolve("libs").resolve("${project.name}-${project.version}.jar"))
    systemProperty("server.port", "9898")
}



