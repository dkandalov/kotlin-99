import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.0"
}

repositories {
    mavenCentral()
    maven ("https://dl.bintray.com/kotlin/kotlin-eap")
}

dependencies {
    testImplementation("com.natpryce:hamkrest:1.8.0.1")
    testImplementation("junit:junit:4.13.2")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "15"
    kotlinOptions.freeCompilerArgs = listOf("-Xstring-concat=indy-with-constants") // see https://kotlinlang.org/docs/reference/whatsnew1420.html
}