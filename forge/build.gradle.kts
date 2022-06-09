plugins {
    id("dev.architectury.loom") version "0.12.0-SNAPSHOT"
}

val shade: Configuration by configurations.creating

dependencies {
    minecraft(group = "com.mojang", name = "minecraft", version = "1.19")
    mappings(loom.officialMojangMappings())
    forge(group = "net.minecraftforge", name = "forge", version = "1.19-41.0.5")
    implementation(project(":chunky-common"))
    shade(project(":chunky-common"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
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
        archiveFileName.set("${rootProject.name.capitalize()}-${project.version}.jar")
    }
}
