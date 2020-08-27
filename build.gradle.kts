
plugins {
    kotlin("jvm") version "1.4.0"
}

dependencies {
    testImplementation("com.natpryce:hamkrest:1.7.0.2")
    testImplementation("junit:junit:4.13")
}

repositories {
    jcenter()
    maven ("https://dl.bintray.com/kotlin/kotlin-eap")
}
