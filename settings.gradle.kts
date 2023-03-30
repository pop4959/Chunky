pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.architectury.dev/")
    }
}

rootProject.name = "chunky"

sequenceOf(
    "common",
    "paper",
    "folia",
    "bukkit",
    "fabric",
    "forge",
    "sponge"
).forEach {
    include("${rootProject.name}-$it")
    project(":${rootProject.name}-$it").projectDir = file(it)
}
