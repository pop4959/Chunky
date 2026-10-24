repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.186-stable")
    compileOnly("com.github.Puremin0rez:WorldBorder:1.19") {
        isTransitive = false
    }
    implementation("org.bstats:bstats-bukkit:3.2.1")
    implementation(project(":chunky-common"))
}

tasks {
    processResources {
        val properties = mapOf(
            "name" to project.property("artifactName")!!,
            "version" to project.version,
            "group" to project.group,
            "author" to project.property("author")!!,
            "description" to project.property("description")!!
        )
        inputs.properties(properties)
        filesMatching("plugin.yml") {
            expand(properties)
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
