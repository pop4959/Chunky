plugins {
    id("dev.architectury.loom") version "0.7.3-SNAPSHOT"
}

dependencies {
    minecraft(group = "com.mojang", name = "minecraft", version = "1.16.5")
    mappings(minecraft.officialMojangMappings())
    forge(group = "net.minecraftforge", name = "forge", version = "1.16.5-36.1.13")
    implementation(project(":chunky-common"))
}

tasks {
    processResources {
        filesMatching("META-INF/mods.toml") {
            expand(
                    "github" to project.property("github"),
                    "id" to rootProject.name,
                    "version" to project.version,
                    "name" to rootProject.name.capitalize(),
                    "author" to project.property("author"),
                    "description" to project.property("description")
            )
        }
    }
    jar {
        manifest {
            attributes(mapOf(
                    "Implementation-Title" to rootProject.name,
                    "Implementation-Version" to project.version,
                    "Implementation-Vendor" to project.property("author")
            ))
        }
    }
    shadowJar {
        dependencies {
            include(project(":chunky-common"))
        }
        exclude("mappings/")
        archiveClassifier.set("dev")
        archiveFileName.set(null as String?)
    }
    remapJar {
        input.set(shadowJar.get().archiveFile)
        archiveFileName.set("${rootProject.name.capitalize()}-${project.version}.jar")
    }
}
