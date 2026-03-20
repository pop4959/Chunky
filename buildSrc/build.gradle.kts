plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal();
    maven("https://maven.fabricmc.net/")
    maven("https://maven.minecraftforge.net")
    maven("https://maven.architectury.dev/")
}

dependencies {
    implementation("com.gradleup.shadow:shadow-gradle-plugin:9.4.0")
    implementation("dev.architectury.loom:dev.architectury.loom.gradle.plugin:1.13-SNAPSHOT")

    // for version catalog inside convention plugin
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

kotlin {
    jvmToolchain(21)
}
