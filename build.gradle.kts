import org.ajoberstar.grgit.Grgit
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val voyagerVer = "1.0.0"
val ktorVersion = "2.3.8"

plugins {
    kotlin("jvm") version "1.9.21"
    id("org.jetbrains.compose") version "1.5.11"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.21"
    id("com.github.gmazzo.buildconfig") version "5.3.5"
    id("org.ajoberstar.grgit") version "5.2.1"
}

group = "moe.styx"
version = "0.0.1"

repositories {
    google()
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.material3:material3:1.5.11")
    implementation("media.kamel:kamel-image:0.9.0")
    implementation("com.aallam.similarity:string-similarity-kotlin:0.1.0")
    implementation("cafe.adriel.voyager:voyager-navigator:$voyagerVer")
    implementation("cafe.adriel.voyager:voyager-screenmodel:$voyagerVer")
    implementation("cafe.adriel.voyager:voyager-tab-navigator:$voyagerVer")
    implementation("cafe.adriel.voyager:voyager-bottom-sheet-navigator:$voyagerVer")
    implementation("cafe.adriel.voyager:voyager-transitions:$voyagerVer")
    implementation("com.russhwolf:multiplatform-settings-no-arg:1.1.1")
    implementation("org.jetbrains.compose.material:material-icons-extended:1.5.11")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

    implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-java:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

    implementation("com.github.oshi:oshi-core:6.4.11")
    implementation("com.github.ajalt.mordant:mordant:2.2.0")
    // https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
    implementation("org.slf4j:slf4j-simple:2.0.9")
    // https://mvnrepository.com/artifact/net.lingala.zip4j/zip4j
    implementation("net.lingala.zip4j:zip4j:2.11.5")

    // Still gotta think about some cross-platform IO stuff
    //implementation("com.soywiz.korlibs.korio:korio:2.2.0")
    // https://mvnrepository.com/artifact/org.jetbrains.compose.material3/material3-desktop
    // implementation("org.jetbrains.compose.material3:material3-desktop:1.4.0")
    //implementation("dev.cbyrne:kdiscordipc:0.2.1")

    implementation("moe.styx:styx-types:0.6")
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
            }
            macOS {
                packageVersion = "1.0.0"
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
}

kotlin {
    jvmToolchain(17)
}

tasks.register("buildExternalDeps") {
    val isWin = System.getProperty("os.name").contains("win", true)
    val projectDir = layout.projectDirectory.asFile.parentFile
    val outDir = File(projectDir, ".temp-deps/styx-db")
    doFirst {
        outDir.deleteRecursively()
        Grgit.clone {
            dir = outDir
            uri = "https://github.com/Vodes/Styx-DB.git"
        }
        val result = kotlin.runCatching {
            ProcessBuilder(listOf(if (isWin) "./gradlew.bat" else "./gradlew", "buildExternalDeps", "publishToMavenLocal"))
                .directory(outDir)
                .inheritIO()
                .start().waitFor()
        }.getOrNull() ?: -1
        if (result != 0) {
            outDir.deleteRecursively()
            throw StopExecutionException()
        }
    }
    doLast { outDir.deleteRecursively() }
}