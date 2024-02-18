import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val voyagerVer = "1.0.0"
val ktorVersion = "2.3.8"

plugins {
    kotlin("jvm") version "1.9.22"
    id("org.jetbrains.compose") version "1.5.12"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
    id("com.github.gmazzo.buildconfig") version "5.3.5"
    id("org.ajoberstar.grgit") version "5.2.1"
}

group = "moe.styx"
version = "0.0.4"

repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://repo.styx.moe/releases")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    // Compose Stuff
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.material3:material3:1.5.12")
    implementation("media.kamel:kamel-image:0.9.0")
    implementation("cafe.adriel.voyager:voyager-navigator:$voyagerVer")
    implementation("cafe.adriel.voyager:voyager-screenmodel:$voyagerVer")
    implementation("cafe.adriel.voyager:voyager-tab-navigator:$voyagerVer")
    implementation("cafe.adriel.voyager:voyager-bottom-sheet-navigator:$voyagerVer")
    implementation("cafe.adriel.voyager:voyager-transitions:$voyagerVer")
    implementation("com.russhwolf:multiplatform-settings-no-arg:1.1.1")
    implementation("org.jetbrains.compose.material:material-icons-extended:1.5.12")

    // Misc
    implementation("com.github.oshi:oshi-core:6.4.11")
    implementation("com.github.ajalt.mordant:mordant:2.2.0")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("net.lingala.zip4j:zip4j:2.11.5")
    implementation("com.github.caoimhebyrne:KDiscordIPC:0.2.2")

    // Styx
    implementation("moe.styx:styx-common:0.0.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

compose.desktop {
    application {
        mainClass = "moe.styx.MainKt"
        buildTypes.release.proguard {
            configurationFiles.from(project.file("proguard.rules"))
        }
        nativeDistributions {
            modules(
                "java.base",
                "java.desktop",
                "java.instrument",
                "java.logging",
                "java.management",
                "java.net.http",
                "java.prefs",
                "jdk.unsupported"
            )
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.AppImage)
            packageName = "Styx 2"
            copyright = "© 2024 Vodes & Styx contributors. All rights reserved."
            vendor = "Vodes & Styx contributors"
            licenseFile.set(project.file("LICENSE"))
            windows {
                menuGroup = "Styx"
                upgradeUuid = System.getenv("STYX_APP_GUID")
                iconFile.set(project.file("src/main/resources/icons/icon.ico"))
            }
            linux {
                iconFile.set(project.file("src/main/resources/icons/icon.png"))
                menuGroup = "AudioVideo;Video"
                shortcut = true
            }
            macOS {
                packageVersion = project.version.toString().let {
                    if (it.startsWith("0.")) it.replaceFirst("0.", "1.") else it
                }
                appStore = false
                iconFile.set(project.file("src/main/resources/icons/icon.icns"))
            }
        }
    }
}

buildConfig {
    val siteURL = System.getenv("STYX_SITEURL")
    buildConfigField("APP_NAME", project.name)
    buildConfigField("APP_VERSION", provider { "${project.version}" })
    buildConfigField("APP_SECRET", System.getenv("STYX_SECRET"))
    buildConfigField("BASE_URL", System.getenv("STYX_BASEURL")) // Example: https://api.company.com
    buildConfigField("SITE_URL", siteURL) // Example: https://company.com
    buildConfigField("IMAGE_URL", System.getenv("STYX_IMAGEURL")) // Example: https://images.company.com
    buildConfigField("SITE", siteURL.split("https://").getOrElse(1) { siteURL })
    buildConfigField("BUILD_TIME", (System.currentTimeMillis() / 1000))
    buildConfigField("VERSION_CHECK_URL", "https://raw.githubusercontent.com/Vodes/Styx-2/master/build.gradle.kts")
    buildConfigField("DISCORD_CLIENT_ID", System.getenv("STYX_DISCORDCLIENT"))
}

kotlin {
    jvmToolchain(17)
}