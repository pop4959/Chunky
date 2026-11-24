pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://repo.codemc.io/repository/relativitymc/")
        exclusiveContent {
            forRepository {
                maven("https://repo.srnyx.com/snapshots/")
            }
            filter {
                includeModule("me.modmuss50", "mod-publish-plugin")
                includeModule("me.modmuss50.mod-publish-plugin", "me.modmuss50.mod-publish-plugin.gradle.plugin")
            }
        }
    }
}

rootProject.name = "chunky"

sequenceOf(
    "nbt",
    "common",
    "paperlib",
    "bukkit",
    "paper",
    "fabric",
    "forge",
    "neoforge",
    "sponge"
).forEach {
    include("${rootProject.name}-$it")
    project(":${rootProject.name}-$it").projectDir = file(it)
}
