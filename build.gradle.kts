import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val voyagerVer = "1.0.0-rc10"
val ktorVersion = "2.3.6"

plugins {
    kotlin("jvm") version "1.9.20"
    id("org.jetbrains.compose") version "1.5.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.20"
}

group = "moe.styx"
version = "1.0"

repositories {
    google()
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("media.kamel:kamel-image:0.8.3")
    implementation("ca.gosyer:accompanist-flowlayout:0.25.2")
    implementation("com.aallam.similarity:string-similarity-kotlin:0.1.0")
    implementation("cafe.adriel.voyager:voyager-navigator:$voyagerVer")
    implementation("cafe.adriel.voyager:voyager-tab-navigator:$voyagerVer")
    implementation("cafe.adriel.voyager:voyager-bottom-sheet-navigator:$voyagerVer")
    implementation("cafe.adriel.voyager:voyager-transitions:$voyagerVer")
    implementation("com.russhwolf:multiplatform-settings-no-arg:1.0.0")
    // https://mvnrepository.com/artifact/org.jetbrains.compose.material/material-icons-extended-desktop
    implementation("org.jetbrains.compose.material:material-icons-extended-desktop:1.5.10")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

    api("io.github.kevinnzou:compose-webview-multiplatform:1.7.2")

    // Still gotta think about some cross-platform IO stuff
    //implementation("com.soywiz.korlibs.korio:korio:2.2.0")

    // https://mvnrepository.com/artifact/org.jetbrains.compose.material3/material3-desktop
    // implementation("org.jetbrains.compose.material3:material3-desktop:1.4.0")

    //implementation("dev.cbyrne:kdiscordipc:0.2.1")
    implementation("pw.vodes:styx-types:0.1")
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.ktor:ktor-client-okhttp-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

compose.desktop {
    application {
        mainClass = "moe.styx.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.AppImage)
            packageName = "Styx-2"
            packageVersion = "1.0.0"
        }
    }
}
kotlin {
    jvmToolchain(17)
}