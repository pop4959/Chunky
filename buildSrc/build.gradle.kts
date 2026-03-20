plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal();
}

dependencies {
    implementation("com.gradleup.shadow:shadow-gradle-plugin:9.4.0")
}

kotlin {
    jvmToolchain(21)
}