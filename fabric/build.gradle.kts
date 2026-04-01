plugins {
    id("org.relativitymc.neo-loom") version "1.16.0-alpha.4"
}

val shade: Configuration by configurations.creating

dependencies {
    minecraft(group = "com.mojang", name = "minecraft", version = "26.1")
    implementation(group = "net.fabricmc", name = "fabric-loader", version = "0.18.5")
    implementation(group = "net.fabricmc.fabric-api", name = "fabric-api", version = "0.144.3+26.1")
    compileOnly(group = "me.lucko", name = "fabric-permissions-api", version = "0.7.0")
    implementation(project(":chunky-common"))
    shade(project(":chunky-common"))
}

loom {
    runs.forEach {
        it.ideConfigGenerated(true)
    }
}

tasks {
    processResources {
        filesMatching("fabric.mod.json") {
            expand(
                "id" to rootProject.name,
                "version" to project.version,
                "name" to project.property("artifactName")!!,
                "description" to project.property("description")!!,
                "author" to project.property("author")!!,
                "github" to project.property("github")!!
            )
        }
    }
    shadowJar {
        configurations = listOf(shade)
        archiveFileName.set("${project.property("artifactName")}-Fabric-${project.version}.jar")
    }
}
