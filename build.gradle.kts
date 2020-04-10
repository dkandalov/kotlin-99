import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4-M1"
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("com.natpryce:hamkrest:1.7.0.2")
    testImplementation("junit:junit:4.13")
}

repositories {
    jcenter()
    maven ("https://dl.bintray.com/kotlin/kotlin-eap")
}

tasks.withType(KotlinCompile::class.java)
    .forEach { it.kotlinOptions { freeCompilerArgs = listOf("-Xnew-inference") } }