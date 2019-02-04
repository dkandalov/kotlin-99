plugins {
    kotlin("jvm") version "1.3.20"
}

dependencies {
    compile(kotlin("stdlib"))
    testCompile("com.natpryce:hamkrest:1.4.2.2")
    testCompile("junit:junit:4.12")
}

repositories {
    jcenter()
}

// New type inference fails in P93.kt
//tasks.withType(KotlinCompile::class.java)
//    .forEach { it.kotlinOptions { freeCompilerArgs = listOf("-Xnew-inference") } }