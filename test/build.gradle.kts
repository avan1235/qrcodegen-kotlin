@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        mainRun {
            mainClass = "in.procyk.qrcodegen.QrCodeTestGeneratorKt"
        }
    }

    linuxX64()
    linuxArm64()

    macosArm64()

    mingwX64()

    sourceSets {
        commonTest.dependencies {
            implementation(project(":lib"))
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

tasks.withType<AbstractTestTask>().configureEach {
    dependsOn(":test:jvmRun")
}