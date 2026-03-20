plugins {
    id("common")
    id("dev.architectury.loom") version "1.13-SNAPSHOT"
}

val shade: Configuration by configurations.creating

repositories {
    maven("https://maven.neoforged.net/releases/")
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    neoForge(libs.neoforge)
    implementation(project(":chunky-common"))
    shade(project(":chunky-common"))
}

tasks {
    processResources {
        val props = mapOf(
            "github" to project.property("github")!!,
            "id" to rootProject.name,
            "version" to project.version,
            "name" to project.property("artifactName")!!,
            "author" to project.property("author")!!,
            "description" to project.property("description")!!
        )
        filesMatching("META-INF/neoforge.mods.toml") {
            expand(props)
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
        archiveFileName.set("${project.property("artifactName")}-NeoForge-${project.version}.jar")
    }
}
