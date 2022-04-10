import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    id("org.spongepowered.gradle.plugin") version "2.0.1"
}

dependencies {
    implementation(project(":chunky-common"))
}

sponge {
    apiVersion("8.0.0")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    license("GPL-3.0")
    plugin(rootProject.name) {
        displayName(rootProject.name.capitalize())
        version("${project.version}")
        entrypoint("${project.group}.${rootProject.name}.ChunkySponge")
        description("${project.property("description")}")
        links {
            homepage("${project.property("github")}")
            source("${project.property("github")}")
            issues("${project.property("github")}/issues")
        }
        contributor("${project.property("author")}") {
            description("Lead Developer")
        }
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
    }
}
