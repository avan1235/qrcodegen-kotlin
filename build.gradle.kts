plugins {
    kotlin("multiplatform") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
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
        commonMain.dependencies {
            implementation("de.cketti.unicode:kotlin-codepoints:0.11.0")
            implementation("io.github.adokky:bitvector:0.9.2")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.8.0")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
        }
        jvmTest.dependencies {
            implementation("io.nayuki:qrcodegen:1.8.0")
        }
    }
}