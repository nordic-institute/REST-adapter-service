plugins {   
    java
    `maven-publish`
    id("org.springframework.boot") version "3.3.3"
}


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
    set("server.port","9898")
}

dependencies {
    // SpringBoot
    implementation("org.springframework.boot:spring-boot-starter-web:3.4.3") 
    implementation("org.springframework.boot:spring-boot-starter-aop:3.3.5")
    implementation("org.springframework.boot:spring-boot-starter-tomcat:3.4.4")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.3.3") {
        exclude (group= "com.vaadin.external.google", module= "android-json")
    }

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
    
    
    testImplementation(libs.org.xmlunit.xmlunit.assertj) {
        constraints {
            implementation("org.assertj:assertj-core:3.16.1")
            implementation("net.bytebuddy:byte-buddy:1.10.5")
        }
    }
    testImplementation(libs.org.apache.tomcat.embed.tomcat.embed.jasper)
    testImplementation(libs.com.github.tomakehurst.wiremock)
    testImplementation(libs.commons.io.commons.io)
    testImplementation(libs.com.github.stefanbirkner.system.rules)
    testImplementation(libs.org.skyscreamer.jsonassert) {
        exclude (group = "com.vaadin.external.google", module = "android-json")
    }
    testImplementation(libs.org.xmlunit.xmlunit.core)
    testImplementation(libs.org.xmlunit.xmlunit.matchers)
    testImplementation(libs.com.jayway.jsonpath.json.path.assert)
    testImplementation(libs.com.jayway.jsonpath.json.path)

    // added to fix runtime errors
    implementation("org.apache.httpcomponents.client5:httpclient5:5.2.1")

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

tasks.processTestResources {
    // Include and filter `application-test-properties/*`
    filesMatching("application-test-properties/*") {
        expand(project.properties) // Enable filtering
    }
    
}

tasks.test {
    useJUnitPlatform()
    // somehow this test wasnt run 
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
