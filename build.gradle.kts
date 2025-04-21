import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.nio.file.Paths

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.buildconfig)
}

group = "moe.styx"
version = "0.1.1-beta2"

// Necessary to have working Windows installers for rc/beta/etc versions.
// Count up by one for every release until a new MINOR version bump.
val subVersionClassifier = 3

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
    implementation(libs.junixsocket)

    // Styx
    implementation(libs.styx.common.compose)
}

val jlinkModules = arrayOf(
    "java.base",
    "java.desktop",
    "java.instrument",
    "java.logging",
    "java.management",
    "java.net.http",
    "java.prefs",
    "jdk.unsupported",
    "jdk.crypto.ec"
)

compose.desktop {
    application {
        mainClass = "moe.styx.MainKt"
        jvmArgs += listOf("-Xmx1250M", "-Xms300M")
        buildTypes.release.proguard {
            configurationFiles.from(project.file("proguard.rules"))
        }
        nativeDistributions {
            modules(*jlinkModules)
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.AppImage)
            packageName = "Styx 2"
            copyright = "© 2024 Vodes & Styx contributors. All rights reserved."
            vendor = "Vodes & Styx contributors"
            licenseFile.set(project.file("LICENSE"))
            windows {
                packageVersion = project.version.toString().split("-")[0] + subVersionClassifier.toString().padStart(4, '0')
                menuGroup = "Styx"
                upgradeUuid = System.getenv("STYX_APP_GUID")
                iconFile.set(project.file("src/main/resources/icons/icon.ico"))
            }
            linux {
                packageVersion = project.version.toString().replace("-", ".")
                iconFile.set(project.file("src/main/resources/icons/icon.png"))
                menuGroup = "AudioVideo;Video"
                shortcut = true
            }
            macOS {
                packageVersion = project.version.toString().let {
                    if (it.startsWith("0.")) it.replaceFirst("0.", "1.") else it
                }.split("-")[0]
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

val optName by lazy { project.name.replace(" ", "-").lowercase() }
val desktopFile by lazy { File(projectDir, "${project.name.replace(" ", "-")}.desktop") }
val cleanupScriptFile by lazy { File(projectDir, "cleanup.sh") }
val buildDir by lazy { File(projectDir, "build") }
val installerOutDir by lazy { Paths.get(buildDir.absolutePath, "compose", "binaries", "installers").toFile().also { it.mkdirs() } }

tasks.register("packageLinuxInstallers") {
    dependsOn("packageReleaseUberJarForCurrentOS")
    doLast {
        val uberJar = Paths.get(buildDir.absolutePath, "compose", "jars").toFile().walkTopDown().find {
            it.isFile && it.name.endsWith("-release.jar", true) && it.name.startsWith(project.name, true)
        } ?: throw Exception("Could not find UberJar!")
        val icon = Paths.get(projectDir.absolutePath, "src").toFile().walkTopDown().find { it.isFile && it.name.equals("icon.png") }
            ?: throw Exception("Could not find app icon!")
        val portableDir = Paths.get(buildDir.absolutePath, "compose", "binaries", "portable").toFile().also { it.mkdirs() }
        uberJar.copyTo(File(portableDir, "$optName-all.jar"), overwrite = true)
        icon.copyTo(File(portableDir, "icon.png"), overwrite = true)
        val jlinkExe =
            getExecutableFromPath("jlink") ?: throw Exception("Please install jlink! It should be in some jdk development package on your distro.")
        exec {
            executable = jlinkExe.absolutePath
            args = listOf(
                "--add-modules",
                jlinkModules.joinToString(","),
                "-G",
                "--no-header-files",
                "--no-man-pages",
                "--compress",
                "1",
                "--output",
                File(portableDir, "jvm").absolutePath
            )
        }
        val fpmExe = getExecutableFromPath("fpm") ?: throw Exception("Please install fpm! https://fpm.readthedocs.io/en/v1.14.0/index.html")
        createNecessaryInstallerFiles()
        val types = listOf("rpm", "deb", "pacman")
        types.forEach {
            exec {
                executable = fpmExe.absolutePath
                args(*getFpmArgs(it, "x86_64", portableDir))
            }
        }
    }
}

fun getFpmArgs(type: String, architecture: String, appImageDir: File): Array<String> {
    return arrayOf(
        "-f",
        "-s",
        "dir",
        "-t",
        type,
        "-v",
        project.version.toString().replace("-", "."),
        "-a",
        architecture,
        "-p",
        installerOutDir.absolutePath,
        "--after-remove",
        cleanupScriptFile.absolutePath,
        "--before-upgrade",
        cleanupScriptFile.absolutePath,
        "--name",
        "styx-2",
        "--license",
        "mpl-2.0",
        "-m",
        "Vodes & Styx contributors",
        "--vendor",
        "Vodes & Styx contributors",
        "--url",
        System.getenv("STYX_SITEURL"),
        "${appImageDir.absolutePath}/.=/opt/$optName",
        "${desktopFile.absolutePath}=/usr/share/applications/"
    )
}

fun createNecessaryInstallerFiles() {
    desktopFile.writeText(
        """
        [Desktop Entry]
        Name=${project.name}
        Comment=Your friendly neighborhood anime streaming service
        Exec=bash -c "/opt/$optName/jvm/bin/java -Xmx1250M -Xms300M -jar /opt/$optName/$optName-all.jar"
        Icon=/opt/$optName/icon.png
        Terminal=false
        Type=Application
        Categories=AudioVideo;Video
        MimeType=
    """.trimIndent()
    )
    cleanupScriptFile.writeText("rm -rf /opt/${optName} ; rm -f /usr/share/applications/${desktopFile.name}")
}

fun getExecutableFromPath(name: String): File? {
    var name = name
    val isWindows = System.getProperty("os.name").contains("win", true)
    if (isWindows && !name.contains(".exe"))
        name = "$name.exe"
    val pathDirs = System.getenv("PATH").split(File.pathSeparator)
        .map { File(it) }.filter { it.exists() && it.isDirectory }

    return pathDirs.flatMap { it.listFiles()?.asList() ?: listOf() }.find { (if (isWindows) it.name else it.nameWithoutExtension).equals(name, true) }
}