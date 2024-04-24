plugins {
    id("dev.architectury.loom") version "1.6-SNAPSHOT"
}

val shade: Configuration by configurations.creating

repositories {
    maven("https://maven.neoforged.net/releases/")
}

dependencies {
    minecraft(group = "com.mojang", name = "minecraft", version = "1.20.2")
    mappings(loom.officialMojangMappings())
    neoForge(group = "net.neoforged", name = "neoforge", version = "20.2.88")
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
        archiveClassifier.set("dev")
        archiveFileName.set(null as String?)
    }
    remapJar {
        inputFile.set(shadowJar.get().archiveFile)
        archiveFileName.set("${project.property("artifactName")}-${project.version}.jar")
    }
}
