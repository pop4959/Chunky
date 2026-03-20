plugins {
    id("common")
    id("dev.architectury.loom") version "1.13-SNAPSHOT"
}

val shade: Configuration by configurations.creating

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    forge(libs.forge)
    implementation(project(":chunky-common"))
    shade(project(":chunky-common"))
}

loom {
    forge {
        mixinConfig("chunky.mixins.json")
    }
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
        filesMatching("META-INF/mods.toml") {
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
        archiveClassifier.set(null as String?)
        archiveFileName.set("${project.property("artifactName")}-Forge-${project.version}.jar")
    }
    remapJar {
        enabled = false
    }
}
