plugins {
    alias(libs.plugins.neo.loom)
}

val shade = configurations.create("shade")

repositories {
    maven("https://maven.minecraftforge.net/")
}

dependencies {
    minecraft(libs.minecraft)
    forgeUserdev(variantOf(libs.forge) { classifier("userdev") })
    compileOnly(libs.mixinextras.common)
    implementation(project(":chunky-common"))
    shade(project(":chunky-common"))
}

loom {
    runs.forEach {
        it.generateRunConfig.set(true)
    }
    mods {
        create("main") {
            sourceSet(project.sourceSets.main.get())
            dependency(project.dependencyFactory.createProjectDependency(":chunky-common"))
        }
    }
    forgeExtraMixinConfigs.add("chunky.mixins.json")
}

tasks {
    jar {
        manifest {
            attributes(
                mapOf(
                    "Implementation-Title" to rootProject.name,
                    "Implementation-Version" to project.version,
                    "Implementation-Vendor" to project.property("author")!!,
                    "MixinConfigs" to "chunky.mixins.json"
                )
            )
        }
    }
    shadowJar {
        configurations = listOf(shade)
        archiveClassifier.set(null as String?)
        archiveFileName.set("${project.property("artifactName")}-Forge-${project.version}.jar")
    }
}
