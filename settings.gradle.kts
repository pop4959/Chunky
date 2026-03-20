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
