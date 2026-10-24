pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.architectury.dev/")
        maven("https://repo.codemc.io/repository/relativitymc/")
    }
}

rootProject.name = "chunky"

sequenceOf(
    "nbt",
    "common",
    "paperlib",
    "bukkit",
    "fabric",
    "forge",
    "neoforge",
    "sponge"
).forEach {
    include("${rootProject.name}-$it")
    project(":${rootProject.name}-$it").projectDir = file(it)
}
