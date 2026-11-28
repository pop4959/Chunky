plugins {
    alias(libs.plugins.hangar.publish)
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.worldborder) {
        isTransitive = false
    }
    implementation(libs.bstats.bukkit)
    implementation(project(":chunky-common"))
}

tasks {
    shadowJar {
        minimize {
            exclude(project(":chunky-common"))
        }
        relocate("org.bstats", "${project.group}.${rootProject.name}.lib.bstats")
        manifest {
            attributes("paperweight-mappings-namespace" to "mojang")
        }
        archiveFileName.set("${project.property("artifactName")}-Paper-${project.version}.jar")
    }
}

hangarPublish {
    publications.register("plugin") {
        version.set("${project.version}")
        id.set("${project.property("hangarProjectId")}")
        channel.set("Release")
        apiKey.set(providers.environmentVariable("PUBLISH_TOKEN_HANGAR"))
        platforms {
            paper {
                jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                platformVersions.set("${project.property("releaseMinecraftVersions")}".split(","))
            }
        }
    }
}
