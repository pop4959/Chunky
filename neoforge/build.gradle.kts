plugins {
    alias(libs.plugins.neo.loom)
}

val shade = configurations.create("shade")

repositories {
    maven("https://maven.neoforged.net/releases/")
}

dependencies {
    minecraft(libs.minecraft)
    forgeUserdev(variantOf(libs.neoforge) { classifier("userdev") })
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
    jar {
        manifest {
            attributes(
                mapOf(
                    "Implementation-Title" to rootProject.name,
                    "Implementation-Version" to project.version,
                    "Implementation-Vendor" to project.property("author")!!
                )
            )
        }
    }
    shadowJar {
        configurations = listOf(shade)
        archiveFileName.set("${project.property("artifactName")}-NeoForge-${project.version}.jar")
    }
}
