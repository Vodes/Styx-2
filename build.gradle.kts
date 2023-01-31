import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.0"
    id("org.jetbrains.compose") version "1.3.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.0"
}

group = "moe.styx"
version = "1.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("com.alialbaali.kamel:kamel-image:0.4.1")
    implementation("io.ktor:ktor-client-content-negotiation:2.2.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.2.1")
    implementation("com.arkivanov.decompose:decompose:1.0.0-beta-01")
    implementation("com.arkivanov.decompose:extensions-compose-jetbrains:1.0.0-beta-01")
    implementation("ca.gosyer:voyager-navigator:1.0.0-rc07")
    implementation("ca.gosyer:voyager-tab-navigator:1.0.0-rc07")
    implementation("ca.gosyer:voyager-bottom-sheet-navigator:1.0.0-rc07")
    implementation("ca.gosyer:voyager-transitions:1.0.0-rc07")
    //implementation("com.russhwolf:multiplatform-settings:1.0.0-RC")
    implementation("com.russhwolf:multiplatform-settings-no-arg:1.0.0-RC")
    // https://mvnrepository.com/artifact/org.jetbrains.compose.material/material-icons-extended-desktop
    implementation("org.jetbrains.compose.material:material-icons-extended-desktop:1.2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    implementation("io.ktor:ktor-client-okhttp-jvm:2.2.2")
    implementation("io.ktor:ktor-client-core-jvm:2.2.2")


    //implementation("dev.cbyrne:kdiscordipc:1.0.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

compose.desktop {
    application {
        mainClass = "moe.styx.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "Styx-2"
            packageVersion = "1.0.0"
        }
    }
}