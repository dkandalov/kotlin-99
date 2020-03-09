plugins {
    kotlin("jvm") version "1.3.60"
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("com.natpryce:hamkrest:1.4.2.2")
    testImplementation("junit:junit:4.12")
}

repositories {
    jcenter()
}

// New type inference fails in P93.kt
//tasks.withType(KotlinCompile::class.java)
//    .forEach { it.kotlinOptions { freeCompilerArgs = listOf("-Xnew-inference") } }