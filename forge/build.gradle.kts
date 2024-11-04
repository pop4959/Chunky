plugins {
    id("dev.architectury.loom") version "1.6-SNAPSHOT"
}

val shade: Configuration by configurations.creating

dependencies {
    minecraft(group = "com.mojang", name = "minecraft", version = "1.21")
    mappings(loom.officialMojangMappings())
    forge(group = "net.minecraftforge", name = "forge", version = "1.21-51.0.8")
    implementation(project(":chunky-common"))
    shade(project(":chunky-common"))
}

tasks {
    processResources {
        filesMatching("META-INF/mods.toml") {
            expand(
                "github" to project.property("github"),
                "id" to rootProject.name,
                "version" to project.version,
                "name" to project.property("artifactName"),
                "author" to project.property("author"),
                "description" to project.property("description")
            )
        }
    }
    jar {
        manifest {
            attributes(
                mapOf(
                    "Implementation-Title" to rootProject.name,
                    "Implementation-Version" to project.version,
                    "Implementation-Vendor" to project.property("author")
                )
            )
        }
    }
    shadowJar {
        configurations = listOf(shade)
        archiveClassifier.set(null as String?)
        archiveFileName.set("${project.property("artifactName")}-Forge-${project.version}.jar")
    }
    remapJar {
        enabled = false
    }
}
