plugins {
    id("common")
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.2-R0.1-SNAPSHOT")
    compileOnly("com.github.Puremin0rez:WorldBorder:1.19") {
        isTransitive = false
    }
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation(project(":chunky-common"))
    implementation(project(":chunky-paper"))
    implementation(project(":chunky-folia"))
}

tasks {
    processResources {
        val props = mapOf(
            "name" to project.property("artifactName")!!,
            "version" to project.version,
            "group" to project.group,
            "author" to project.property("author")!!,
            "description" to project.property("description")!!,
        )
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
    shadowJar {
        minimize {
            exclude(project(":chunky-common"))
            exclude(project(":chunky-paper"))
            exclude(project(":chunky-folia"))
        }
        relocate("org.bstats", "${project.group}.${rootProject.name}.lib.bstats")
        manifest {
            attributes("paperweight-mappings-namespace" to "mojang")
        }
        archiveFileName.set("${project.property("artifactName")}-Bukkit-${project.version}.jar")
    }
}
