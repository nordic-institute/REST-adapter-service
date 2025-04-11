plugins {
    java
    `maven-publish`
    id("org.springframework.boot") version "3.3.3"
}

sourceSets

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


//sourceSets {
//    getByName("main").java.srcDirs("src/src/java")
//    getByName("main").resources.srcDirs("src/src/resources")
//}

ext {
    set("xrd4j.version", "0.6.0")
    set("java.version", "21")
    set("jdk.version", "21")
    set("xmlunit.version", "2.7.0")
    set("failsafe.version", "2.19.1")
    set("project.build.sourceEncoding", "UTF-8")
    set("project.build.resourceEncoding", "UTF-8")
    set("project.reporting.outputEncoding", "UTF-8")
    set("tomcat.version", "9.0.37")
    set("app.home", "/var/lib/tomcat/webapps")
    // set("sonar.junit.reportPaths", "target/failsafe-reports,target/surefire-reports")
    set("server.port", "9898")
}

dependencies {
    // SpringBoot
    implementation("org.springframework.boot:spring-boot-starter-web:3.4.3")
    implementation("org.springframework.boot:spring-boot-starter-aop:3.3.5")
    implementation("org.springframework.boot:spring-boot-starter-tomcat:3.4.4")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.4.4") {
        exclude(group = "com.vaadin.external.google", module = "android-json")
        exclude(group = "org.skyscreamer", module = "jsonassert")
        exclude(group = "junit")
    }
//    implementation("org.springframework.boot:spring-boot-starter-tomcat:3.3.3")
//    implementation("jakarta.servlet:jakarta.servlet-api:6.0.0")

    // xrd4j
    implementation("org.niis.xrd4j:common:0.6.0")
    implementation("org.niis.xrd4j:client:0.6.0")
    implementation("org.niis.xrd4j:server:0.6.0")
    implementation("org.niis.xrd4j:rest:0.6.0")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    testImplementation("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")


    implementation("org.assertj:assertj-core:3.24.2")
    implementation("net.bytebuddy:byte-buddy:1.10.5")

//    testImplementation("junit:junit:4.13.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation(libs.org.xmlunit.xmlunit.assertj)
    testImplementation("org.apache.tomcat.embed:tomcat-embed-jasper:10.1.13")
    testImplementation("com.github.tomakehurst:wiremock:2.27.2") {
        exclude(group = "com.vaadin.external.google", module = "android-json")
    }
    testImplementation(libs.commons.io.commons.io)
    testImplementation(libs.com.github.stefanbirkner.system.rules)
    testImplementation("org.skyscreamer:jsonassert:1.5.1") {
        exclude(group = "com.vaadin.external.google", module = "android-json")
    }
    testImplementation(libs.org.xmlunit.xmlunit.core)
    testImplementation(libs.org.xmlunit.xmlunit.matchers)
    testImplementation(libs.com.jayway.jsonpath.json.path.assert)
    testImplementation(libs.com.jayway.jsonpath.json.path)
    //    testImplementation("junit:junit:4.12")


    //added to fix runtime errors
    implementation("org.apache.httpcomponents.client5:httpclient5:5.4.1")
    testImplementation("org.apache.httpcomponents.client5:httpclient5:5.4.1")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

//    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.12.0")

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

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
    options.annotationProcessorPath = configurations.annotationProcessor.get()

}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}

val isEncrypted = project.hasProperty("encrypted")
val adapterProfileDir = if (isEncrypted) "encrypted" else "plaintext"

tasks.processResources {
    from("src/main/profiles/$adapterProfileDir/") {}
}

tasks.test {
    useJUnitPlatform()
    System.setProperty("project.projectDir", project.projectDir.toString())
    exclude("org/niis/xroad/restadapterservice/ConsumerGatewayIT.class")
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
