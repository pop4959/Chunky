plugins {
    id("dev.architectury.loom") version "1.9-SNAPSHOT"
}

val shade: Configuration by configurations.creating

dependencies {
    minecraft(group = "com.mojang", name = "minecraft", version = "1.21.5")
    mappings(loom.officialMojangMappings())
    modImplementation(group = "net.fabricmc", name = "fabric-loader", version = "0.16.10")
    modImplementation(group = "net.fabricmc.fabric-api", name = "fabric-api", version = "0.119.5+1.21.5")
    modCompileOnly(group = "me.lucko", name = "fabric-permissions-api", version = "0.3.3")
    implementation(project(":chunky-common"))
    shade(project(":chunky-common"))
}

tasks {
    processResources {
        filesMatching("fabric.mod.json") {
            expand(
                "id" to rootProject.name,
                "version" to project.version,
                "name" to project.property("artifactName"),
                "description" to project.property("description"),
                "author" to project.property("author"),
                "github" to project.property("github")
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
        archiveFileName.set("${project.property("artifactName")}-Fabric-${project.version}.jar")
    }
}
