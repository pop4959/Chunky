plugins {
    id("org.relativitymc.neo-loom") version "1.17.11"
}

val shade = configurations.create("shade")

dependencies {
    minecraft("com.mojang:minecraft:26.1")
    implementation("net.fabricmc:fabric-loader:0.18.5")
    implementation("net.fabricmc.fabric-api:fabric-api:0.149.0+26.1.2")
    implementation(project(":chunky-common"))
    shade(project(":chunky-common"))
}

loom {
    runs.forEach {
        it.generateRunConfig.set(true)
    }
}

tasks {
    processResources {
        val properties = mapOf(
            "id" to rootProject.name,
            "version" to project.version,
            "name" to project.property("artifactName")!!,
            "description" to project.property("description")!!,
            "author" to project.property("author")!!,
            "github" to project.property("github")!!
        )
        inputs.properties(properties)
        filesMatching("fabric.mod.json") {
            expand(properties)
        }
    }
    shadowJar {
        configurations = listOf(shade)
        archiveFileName.set("${project.property("artifactName")}-Fabric-${project.version}.jar")
    }
}
