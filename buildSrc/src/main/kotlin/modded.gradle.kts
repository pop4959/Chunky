import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("common")
    id("dev.architectury.loom")
}

val libs = the<LibrariesForLibs>()
val moddedExtension: ModdedExtension = extensions.create("modded", ModdedExtension::class.java)

val shade: Configuration by configurations.creating

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
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
        moddedExtension.processMatching?.let {
            filesMatching(it) {
                expand(props)
            }
        }
    }
    shadowJar {
        configurations = listOf(shade)
        archiveClassifier.set("dev")
    }
    jar {
        manifest {
            if (moddedExtension.forge) {
                attributes(
                    mapOf(
                        "Implementation-Title" to rootProject.name,
                        "Implementation-Version" to project.version,
                        "Implementation-Vendor" to project.property("author")
                    )
                )
            }
        }
    }
}

abstract class ModdedExtension @Inject constructor(objects: ObjectFactory) {
    var processMatching: String? = null
    var forge: Boolean = false
}
