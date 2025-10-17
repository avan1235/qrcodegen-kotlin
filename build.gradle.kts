group = "in.procyk"

plugins {
    kotlin("multiplatform") version "2.2.20" apply false
    kotlin("plugin.serialization") version "2.2.20" apply false
    id("io.github.adokky.quick-mpp") version "0.18" apply false
    id("io.github.adokky.quick-publish") version "0.18" apply false
}

subprojects {
    group = "in.procyk"
}
