import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    alias(libs.plugins.sponge)
    alias(libs.plugins.ore)
}

dependencies {
    compileOnly(libs.annotations)
    implementation(project(":chunky-common"))
}

sponge {
    apiVersion(libs.versions.sponge.api.get())
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    license("GPL-3.0")
    plugin(rootProject.name) {
        displayName("${project.property("artifactName")}")
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

oreDeployment {
    apiKey().set(providers.environmentVariable("PUBLISH_TOKEN_ORE"))
    defaultPublication {
        projectId.set("${project.property("oreProjectId")}")
        createForumPost.set(false)
        versionBody.set("")
        publishArtifacts.setFrom(tasks.shadowJar)
    }
}

tasks {
    shadowJar {
        archiveFileName.set("${project.property("artifactName")}-Sponge-${project.version}.jar")
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-proc:none")
}
