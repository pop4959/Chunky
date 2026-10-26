plugins {
    id("org.relativitymc.neo-loom") version "1.17.11"
}

val shade = configurations.create("shade")

repositories {
    maven("https://maven.minecraftforge.net/")
}

dependencies {
    minecraft("com.mojang:minecraft:26.1")
    forgeUserdev("net.minecraftforge:forge:26.1-62.0.9:userdev")
    implementation(project(":chunky-common"))
    shade(project(":chunky-common"))
}

loom {
    runs.forEach {
        it.generateRunConfig.set(true)
    }
    mods {
        create("main") {
            sourceSet(project.sourceSets.main.get())
            dependency(project.dependencyFactory.createProjectDependency(":chunky-common"))
        }
    }
}

tasks {
    processResources {
        val properties = mapOf(
            "github" to project.property("github")!!,
            "id" to rootProject.name,
            "version" to project.version,
            "name" to project.property("artifactName")!!,
            "author" to project.property("author")!!,
            "description" to project.property("description")!!
        )
        inputs.properties(properties)
        filesMatching("META-INF/mods.toml") {
            expand(properties)
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
