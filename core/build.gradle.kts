plugins {
    id("io.github.adokky.quick-mpp")
    id("io.github.adokky.quick-publish")
}

version = "0.0.1"

dependencies {
    commonMainImplementation("de.cketti.unicode:kotlin-codepoints:0.11.0")
    commonMainImplementation("io.github.adokky:bitvector:0.9.2")
}

mavenPublishing {
    coordinates(artifactId = "qrcodegen")
    pom {
        name = "qrcodegen"
        description = "QR code generator for Kotlin Multiplatform "
        inceptionYear = "2025"
        developers {
            developer {
                id = "avan1235"
                name = "Maciej Procyk"
                url = "https://github.com/avan1235"
            }
        }
    }
}