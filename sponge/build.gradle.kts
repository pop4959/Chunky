import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.PluginDependency

plugins {
    id("org.spongepowered.gradle.plugin") version "1.1.1"
}

dependencies {
    implementation(project(":chunky-common"))
}

sponge {
    apiVersion("8.0.0")
    plugin(rootProject.name) {
        loader(PluginLoaders.JAVA_PLAIN)
        displayName(rootProject.name.capitalize())
        mainClass("${project.group}.${rootProject.name}.ChunkySponge")
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
