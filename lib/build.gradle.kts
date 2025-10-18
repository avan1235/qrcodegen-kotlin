import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka")
    id("io.github.adokky.quick-mpp")
    id("com.vanniktech.maven.publish")
}

dependencies {
    commonMainImplementation("de.cketti.unicode:kotlin-codepoints:0.11.0")
    commonMainImplementation("io.github.adokky:bitvector:0.9.2")
}

mavenPublishing {
    configure(
        KotlinMultiplatform(
            javadocJar = JavadocJar.Dokka("dokkaHtml"),
            sourcesJar = true,
        )
    )

    publishToMavenCentral()

    signAllPublications()

    coordinates(artifactId = "qrcodegen")

    pom {
        val githubRepoUrl = "github.com/avan1235/qrcodegen-kotlin"
        name = "qrcodegen"
        description = "QR code generator for Kotlin Multiplatform "
        inceptionYear = "2025"
        url = "https://$githubRepoUrl"
        developers {
            developer {
                id = "avan1235"
                name = "Maciej Procyk"
                email = "maciej@procyk.in"
                url = "https://github.com/avan1235"
                organizationUrl = "https://procyk.in"
            }
        }
        scm {
            url = "https://$githubRepoUrl"
            connection = "scm:git:git://$githubRepoUrl.git"
            developerConnection = "scm:git:git://$githubRepoUrl.git"
        }
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
    }
}

afterEvaluate {
    tasks.withType<AbstractPublishToMaven>().configureEach {
        dependsOn(":test:allTests")
    }
}
