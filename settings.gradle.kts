pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}
rootProject.name = "Styx 2"

val localCommonCompose = file("../Styx-Common-Compose")
if (localCommonCompose.isDirectory) {
    includeBuild(localCommonCompose) {
        dependencySubstitution {
            substitute(module("moe.styx:styx-common-compose-jvm"))
                .using(project(":styx-common-compose"))
        }
    }
}
