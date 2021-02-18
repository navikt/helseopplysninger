import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer

object Version {
    const val hapi = "5.3.0"
    const val spring_boot_version = "2.4.2"
    const val postgresql = "42.2.18"
    const val h2 = "1.4.200"
}

plugins {
    java
    application
    id("org.springframework.boot") version "2.4.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

application {
    mainClassName = "ca.uhn.fhir.jpa.starter.Application"
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
}

dependencies {
    implementation("ca.uhn.hapi.fhir:hapi-fhir-jpaserver-base:${Version.hapi}") {
        exclude(group = "org.springframework", module = "spring-jcl")
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation("ca.uhn.hapi.fhir:hapi-fhir-jpaserver-cql:${Version.hapi}")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-jpaserver-mdm:${Version.hapi}")
    implementation("org.springframework.boot:spring-boot-autoconfigure:${Version.spring_boot_version}")
    runtimeOnly("org.springframework.boot:spring-boot-starter-web:${Version.spring_boot_version}")
    //runtimeOnly("org.apache.commons:commons-dbcp2:2.7.0")
    runtimeOnly("org.postgresql:postgresql:${Version.postgresql}")
    runtimeOnly("com.h2database:h2:${Version.h2}")
    runtimeOnly("org.springframework.boot:spring-boot-starter-security:${Version.spring_boot_version}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<ShadowJar> {
    isZip64 = true
    transform(com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer::class.java) {
        setPath("META-INF/cxf")
        include("bus-extensions.txt")
    }
    transform(PropertiesFileTransformer::class.java) {
        paths = listOf("META-INF/spring.factories")
        mergeStrategy = "append"
    }
    mergeServiceFiles()
}