pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net")
        maven("https://repo.redlance.org/public/") // https://github.com/architectury/architectury-loom/issues/271
        maven("https://maven.architectury.dev/")
    }
}

rootProject.name = "chunky"

sequenceOf(
    "nbt",
    "common",
    "paper",
    "folia",
    "bukkit",
    "fabric",
    "forge",
    "neoforge",
    "sponge"
).forEach {
    include("${rootProject.name}-$it")
    project(":${rootProject.name}-$it").projectDir = file(it)
}
