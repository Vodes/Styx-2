import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    id("org.jetbrains.compose") version "1.2.0"
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
    implementation("io.ktor:ktor-client-core:2.1.3")
    implementation("io.ktor:ktor-client-okhttp:2.1.3")
    implementation("com.arkivanov.decompose:decompose:1.0.0-beta-01")
    implementation("com.arkivanov.decompose:extensions-compose-jetbrains:1.0.0-beta-01")
    implementation("ca.gosyer:voyager-navigator:1.0.0-rc07")
    implementation("ca.gosyer:voyager-tab-navigator:1.0.0-rc07")
    implementation("ca.gosyer:voyager-bottom-sheet-navigator:1.0.0-rc07")
    implementation("ca.gosyer:voyager-transitions:1.0.0-rc07")
    //implementation("com.russhwolf:multiplatform-settings:1.0.0-RC")
    implementation("com.russhwolf:multiplatform-settings-no-arg:1.0.0-RC")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "Styx-2"
            packageVersion = "1.0.0"
        }
    }
}