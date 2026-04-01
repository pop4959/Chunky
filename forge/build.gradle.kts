plugins {
    id("org.relativitymc.neo-loom") version "1.16.0-alpha.4"
}

val shade: Configuration by configurations.creating

repositories {
    maven("https://maven.minecraftforge.net/")
}

dependencies {
    minecraft(group = "com.mojang", name = "minecraft", version = "26.1")
    forgeUserdev(group = "net.minecraftforge", name = "forge", version = "26.1-62.0.9", classifier = "userdev")
    implementation(project(":chunky-common"))
    shade(project(":chunky-common"))
}

loom {
    runs.forEach {
        it.ideConfigGenerated(true)
    }
    mods {
        create("main") {
            sourceSet(project.sourceSets.main.get())
            dependency(project.dependencyFactory.create(project(":chunky-common")))
        }
    }
}

tasks {
    processResources {
        filesMatching("META-INF/mods.toml") {
            expand(
                "github" to project.property("github")!!,
                "id" to rootProject.name,
                "version" to project.version,
                "name" to project.property("artifactName")!!,
                "author" to project.property("author")!!,
                "description" to project.property("description")!!
            )
        }
    }
    jar {
        manifest {
            attributes(
                mapOf(
                    "Implementation-Title" to rootProject.name,
                    "Implementation-Version" to project.version,
                    "Implementation-Vendor" to project.property("author")!!,
                    "MixinConfigs" to "chunky.mixins.json"
                )
            )
        }
    }
    shadowJar {
        configurations = listOf(shade)
        archiveClassifier.set(null as String?)
        archiveFileName.set("${project.property("artifactName")}-Forge-${project.version}.jar")
    }
}
