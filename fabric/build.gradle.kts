plugins {
    id("fabric-loom") version "0.8-SNAPSHOT"
}

dependencies {
    minecraft(group = "com.mojang", name = "minecraft", version = "1.17.1")
    mappings(group = "net.fabricmc", name = "yarn", version = "1.17.1+build.31", classifier = "v2")
    modImplementation(group = "net.fabricmc", name = "fabric-loader", version = "0.11.6")
    modImplementation(group = "net.fabricmc.fabric-api", name = "fabric-api", version = "0.37.1+1.17")
    implementation(project(":chunky-common"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
}

tasks {
    processResources {
        filesMatching("fabric.mod.json") {
            expand(
                    "id" to rootProject.name,
                    "version" to project.version,
                    "name" to rootProject.name.capitalize(),
                    "description" to project.property("description"),
                    "author" to project.property("author"),
                    "github" to project.property("github")
            )
        }
    }
    shadowJar {
        dependencies {
            include(project(":chunky-common"))
        }
        exclude("mappings/")
        archiveClassifier.set("dev")
        archiveFileName.set(null as String?)
    }
    remapJar {
        input.set(shadowJar.get().archiveFile)
        archiveFileName.set("${rootProject.name.capitalize()}-${project.version}.jar")
    }
}
