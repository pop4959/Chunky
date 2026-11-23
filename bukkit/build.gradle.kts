repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly(libs.spigot.api)
    compileOnly(libs.worldborder) {
        isTransitive = false
    }
    implementation(libs.bstats.bukkit)
    implementation(project(":chunky-common"))
    implementation(project(":chunky-paperlib"))
}

tasks {
    processResources {
        val properties = mapOf(
            "name" to project.property("artifactName")!!,
            "version" to project.version,
            "group" to project.group,
            "author" to project.property("author")!!,
            "description" to project.property("description")!!
        )
        inputs.properties(properties)
        filesMatching("plugin.yml") {
            expand(properties)
        }
    }
    shadowJar {
        minimize {
            exclude(project(":chunky-common"))
            exclude(project(":chunky-paperlib"))
        }
        relocate("org.bstats", "${project.group}.${rootProject.name}.lib.bstats")
        manifest {
            attributes("paperweight-mappings-namespace" to "mojang")
        }
        archiveFileName.set("${project.property("artifactName")}-Bukkit-${project.version}.jar")
    }
}
