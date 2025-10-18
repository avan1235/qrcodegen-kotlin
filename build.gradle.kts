plugins {
    kotlin("multiplatform") version "2.2.20" apply false
    kotlin("plugin.serialization") version "2.2.20" apply false
    id("org.jetbrains.dokka") version "2.0.0" apply false
    id("io.github.adokky.quick-mpp") version "0.19" apply false
    id("com.vanniktech.maven.publish") version "0.34.0" apply false
}

allprojects {
    group = "in.procyk"
    version = "0.0.1"
}
