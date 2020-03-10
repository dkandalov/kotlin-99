import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.70"
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("com.natpryce:hamkrest:1.4.2.2")
    testImplementation("junit:junit:4.12")
}

repositories {
    jcenter()
}

tasks.withType(KotlinCompile::class.java)
    .forEach { it.kotlinOptions { freeCompilerArgs = listOf("-Xnew-inference") } }