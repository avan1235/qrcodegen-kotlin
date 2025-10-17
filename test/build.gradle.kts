plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

group = "in.procyk"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm()

    linuxX64()
    linuxArm64()

    macosX64()
    macosArm64()

    sourceSets {
        commonTest.dependencies {
            implementation(project(":core"))
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.8.0")
        }
        jvmMain.dependencies {
            implementation("io.nayuki:qrcodegen:1.8.0")
        }
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
        }
    }
}