plugins {
    id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT"
}

val shade: Configuration by configurations.creating

dependencies {
    minecraft(group = "com.mojang", name = "minecraft", version = "26.1-pre-2")
//    mappings(loom.officialMojangMappings())
    implementation(group = "net.fabricmc", name = "fabric-loader", version = "0.18.4")
    implementation(group = "net.fabricmc.fabric-api", name = "fabric-api", version = "0.143.14+26.1")
//    modCompileOnly(group = "me.lucko", name = "fabric-permissions-api", version = "0.6.1")
    implementation(project(":chunky-common"))
    shade(project(":chunky-common"))
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
//    remapJar {
//        inputFile.set(shadowJar.get().archiveFile)
//        archiveFileName.set("${project.property("artifactName")}-Fabric-${project.version}.jar")
//    }
}
