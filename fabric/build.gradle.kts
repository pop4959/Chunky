plugins {
    id("dev.architectury.loom") version "1.5-SNAPSHOT"
}

val shade: Configuration by configurations.creating

dependencies {
    minecraft(group = "com.mojang", name = "minecraft", version = "1.20")
    mappings(group = "net.fabricmc", name = "yarn", version = "1.20+build.1", classifier = "v2")
    modImplementation(group = "net.fabricmc", name = "fabric-loader", version = "0.14.21")
    modImplementation(group = "net.fabricmc.fabric-api", name = "fabric-api", version = "0.83.0+1.20")
    modCompileOnly(group = "me.lucko", name = "fabric-permissions-api", version = "0.2-SNAPSHOT")
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
        archiveFileName.set("${project.property("artifactName")}-${project.version}.jar")
    }
}
