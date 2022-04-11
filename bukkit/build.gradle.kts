repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://papermc.io/repo/repository/maven-public")
    maven("https://repo.mikeprimm.com")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly(group = "org.spigotmc", name = "spigot-api", version = "1.18.2-R0.1-SNAPSHOT")
    compileOnly(group = "us.dynmap", name = "DynmapCoreAPI", version = "3.3")
    compileOnly(group = "com.github.BlueMap-Minecraft", name = "BlueMapAPI", version = "v1.7.0")
    compileOnly(group = "xyz.jpenilla", name = "squaremap-api", version = "1.1.2")
    compileOnly(group = "com.github.Brettflan", name = "WorldBorder", version = "44f388f3ba") {
        isTransitive = false
    }
    implementation(group = "io.papermc", name = "paperlib", version = "1.0.8-SNAPSHOT")
    implementation(group = "org.bstats", name = "bstats-bukkit", version = "3.0.0")
    implementation(project(":chunky-common"))
}

tasks {
    processResources {
        filesMatching("plugin.yml") {
            expand(
                "name" to rootProject.name.capitalize(),
                "version" to project.version,
                "group" to project.group,
                "author" to project.property("author"),
                "description" to project.property("description"),
            )
        }
    }
    shadowJar {
        minimize {
            exclude(project(":chunky-common"))
        }
        relocate("io.papermc.lib", "${project.group}.${rootProject.name}.lib.paperlib")
        relocate("org.bstats", "${project.group}.${rootProject.name}.lib.bstats")
    }
}
