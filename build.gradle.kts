import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.10"
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
    kotlinOptions.jvmTarget = "17"
    kotlinOptions.freeCompilerArgs = listOf(
        "-Xstring-concat=indy-with-constants", // see https://kotlinlang.org/docs/whatsnew1420.html#invokedynamic-string-concatenation
        "-Xuse-k2" // see https://kotlinlang.org/docs/whatsnew17.html#new-kotlin-k2-compiler-for-the-jvm-in-alpha
    )
}