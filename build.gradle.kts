import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.20"
}

dependencies {
    testImplementation("com.natpryce:hamkrest:1.7.0.2")
    testImplementation("junit:junit:4.13")
}

repositories {
    jcenter()
    maven ("https://dl.bintray.com/kotlin/kotlin-eap")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "15"
    kotlinOptions.useIR = true // see https://kotlinlang.org/docs/reference/whatsnew14.html#new-jvm-ir-backend
    kotlinOptions.freeCompilerArgs = listOf("-Xstring-concat=indy-with-constants") // see https://kotlinlang.org/docs/reference/whatsnew1420.html
}