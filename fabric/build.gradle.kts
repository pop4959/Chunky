plugins {
    id("common")
    id("dev.architectury.loom") version "1.13-SNAPSHOT"
}

val shade: Configuration by configurations.creating

dependencies {
    minecraft("com.mojang:minecraft:1.21.11")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.18.1")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.139.4+1.21.11")
    modCompileOnly("me.lucko:fabric-permissions-api:0.6.1")
    implementation(project(":chunky-common"))
    shade(project(":chunky-common"))
}

tasks {
    processResources {
        val props = mapOf(
            "id" to rootProject.name,
            "version" to project.version,
            "name" to project.property("artifactName")!!,
            "description" to project.property("description")!!,
            "author" to project.property("author")!!,
            "github" to project.property("github")!!
        )
        filesMatching("fabric.mod.json") {
            expand(props)
        }
    }
    shadowJar {
        configurations = listOf(shade)
        archiveClassifier.set("dev")
        archiveFileName.set(null as String?)
    }
    remapJar {
        inputFile.set(shadowJar.get().archiveFile)
        archiveFileName.set("${project.property("artifactName")}-Fabric-${project.version}.jar")
    }
}
