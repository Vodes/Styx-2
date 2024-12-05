import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.buildconfig)
}

group = "moe.styx"
version = "0.0.9"

repositories {
    google()
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://repo.styx.moe/releases")
    maven("https://repo.styx.moe/snapshots")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    // Compose Stuff
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)

    // Misc
    implementation(libs.slf4j.simple)
    implementation(libs.zip4j)
    implementation(libs.kdiscord.ipc)
    implementation(libs.okio)

    // Styx
    implementation(libs.styx.common.compose)
}

compose.desktop {
    application {
        mainClass = "moe.styx.MainKt"
        jvmArgs += listOf("-Xmx1250M", "-Xms300M")
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
    buildConfigField("VERSION_CHECK_URL", "https://api.github.com/repos/Vodes/Styx-2/tags")
    buildConfigField("DISCORD_CLIENT_ID", "686174250259709983")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.JETBRAINS)
    }
}