repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly(group = "io.papermc.paper", name = "paper-api", version = "26.3.build.186-stable")
    compileOnly(group = "com.github.Puremin0rez", name = "WorldBorder", version = "1.19") {
        isTransitive = false
    }
    implementation(group = "org.bstats", name = "bstats-bukkit", version = "3.0.2")
    implementation(project(":chunky-common"))
}

tasks {
    processResources {
        filesMatching("plugin.yml") {
            expand(
                "name" to project.property("artifactName")!!,
                "version" to project.version,
                "group" to project.group,
                "author" to project.property("author")!!,
                "description" to project.property("description")!!,
            )
        }
    }
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
