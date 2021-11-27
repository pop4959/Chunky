repositories {
    maven("https://papermc.io/repo/repository/maven-public")
}

dependencies {
    compileOnly(group = "org.spigotmc", name = "spigot-api", version = "1.17.1-R0.1-SNAPSHOT")
    implementation(group = "io.papermc", name = "paperlib", version = "1.0.8-SNAPSHOT")
    implementation(group = "org.bstats", name = "bstats-bukkit", version = "2.2.1")
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
