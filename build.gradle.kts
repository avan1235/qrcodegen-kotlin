plugins {
    java
    kotlin("jvm") version "2.2.20"
}

group = "in.procyk"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.adokky:bitvector:0.9")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}