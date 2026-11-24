import me.modmuss50.mpp.ReleaseType

plugins {
    id("java-library")
    id("maven-publish")
    alias(libs.plugins.shadow)
    alias(libs.plugins.mod.publish)
}

val buildNumber = commitsSinceLastTag()

subprojects {
    plugins.apply("java-library")
    plugins.apply("maven-publish")
    plugins.apply("com.gradleup.shadow")

    group = "${project.property("group")}"
    version = "${project.property("version")}.$buildNumber"

    repositories {
        mavenCentral()
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
        withSourcesJar()
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
            options.release = 25
            options.compilerArgs.add("-Xlint:deprecation,removal")
        }
        jar {
            archiveClassifier.set("noshade")
        }
        shadowJar {
            archiveClassifier.set("")
            archiveFileName.set("${project.property("artifactName")}-${project.version}.jar")
        }
        build {
            dependsOn(shadowJar)
        }
    }

    publishing {
        repositories {
            if (project.hasProperty("mavenUsername") && project.hasProperty("mavenPassword")) {
                maven {
                    credentials {
                        username = "${project.property("mavenUsername")}"
                        password = "${project.property("mavenPassword")}"
                    }
                    url = uri("https://repo.codemc.io/repository/maven-releases/")
                }
            }
        }
        publications {
            create<MavenPublication>("maven") {
                groupId = "${project.group}"
                artifactId = project.name
                version = "${project.version}"
                from(components["java"])
            }
        }
    }
}

val releaseVersion = "${project.property("version")}.$buildNumber"
val releaseMinecraftVersions = "${project.property("releaseMinecraftVersions")}".split(",")
val releaseSpongeVersions = "${project.property("releaseSpongeVersions")}".split(",")

publishMods {
    version.set(releaseVersion)
    displayName.set("Chunky $releaseVersion")
    changelog.set("")
    type.set(ReleaseType.STABLE)

    val modrinthOptions = modrinthOptions {
        accessToken.set(providers.environmentVariable("PUBLISH_TOKEN_MODRINTH"))
        projectId.set("${project.property("modrinthProjectId")}")
        minecraftVersions.addAll(releaseMinecraftVersions)
    }
    val curseforgeOptions = curseforgeOptions {
        accessToken.set(providers.environmentVariable("PUBLISH_TOKEN_CURSEFORGE"))
        minecraftVersions.addAll(releaseMinecraftVersions)
        client.set(true)
        server.set(true)
    }

    modrinth("modrinthSponge") {
        from(modrinthOptions)
        file.set(shadowJarFile("sponge"))
        modLoaders.add("sponge")
        minecraftVersions.set(releaseSpongeVersions)
    }
    modrinth("modrinthBukkit") {
        from(modrinthOptions)
        file.set(shadowJarFile("bukkit"))
        modLoaders.addAll("bukkit", "spigot")
    }
    modrinth("modrinthPaper") {
        from(modrinthOptions)
        file.set(shadowJarFile("paper"))
        modLoaders.addAll("paper", "folia")
    }
    modrinth("modrinthForge") {
        from(modrinthOptions)
        file.set(shadowJarFile("forge"))
        modLoaders.add("forge")
    }
    modrinth("modrinthNeoForge") {
        from(modrinthOptions)
        file.set(shadowJarFile("neoforge"))
        modLoaders.add("neoforge")
    }
    modrinth("modrinthFabric") {
        from(modrinthOptions)
        file.set(shadowJarFile("fabric"))
        modLoaders.add("fabric")
        requires {
            id.set("${project.property("modrinthFabricApiId")}")
        }
    }

    curseforge("curseforgeBukkit") {
        accessToken.set(providers.environmentVariable("PUBLISH_TOKEN_CURSEFORGE"))
        projectId.set("${project.property("devBukkitProjectId")}")
        file.set(shadowJarFile("bukkit"))
        modLoaders.add("bukkit")
        minecraftVersions.addAll(releaseMinecraftVersions)
    }
    curseforge("curseforgeForge") {
        from(curseforgeOptions)
        projectId.set("${project.property("curseforgeForgeProjectId")}")
        file.set(shadowJarFile("forge"))
        modLoaders.add("forge")
    }
    curseforge("curseforgeNeoForge") {
        from(curseforgeOptions)
        projectId.set("${project.property("curseforgeForgeProjectId")}")
        file.set(shadowJarFile("neoforge"))
        modLoaders.add("neoforge")
    }
    curseforge("curseforgeFabric") {
        from(curseforgeOptions)
        projectId.set("${project.property("curseforgeFabricProjectId")}")
        file.set(shadowJarFile("fabric"))
        modLoaders.add("fabric")
    }
}

tasks {
    val publishOrder = listOf(
        "publishModrinthSponge",
        "publishModrinthBukkit",
        "publishModrinthPaper",
        "publishModrinthForge",
        "publishModrinthNeoForge",
        "publishModrinthFabric",
        "publishCurseforgeBukkit",
        "publishCurseforgeForge",
        "publishCurseforgeNeoForge",
        "publishCurseforgeFabric"
    )
    publishOrder.forEachIndexed { index, task ->
        named(task) {
            mustRunAfter(publishOrder.take(index))
        }
    }
}

fun shadowJarFile(module: String) =
    project(":chunky-$module").tasks.named<AbstractArchiveTask>("shadowJar").flatMap { it.archiveFile }

fun commitsSinceLastTag(): String {
    val tagDescription = providers.exec {
        commandLine("git", "describe", "--tags")
        isIgnoreExitValue = true
    }.standardOutput.asText.get()
    return tagDescription.split('-').getOrElse(1) { "0" }
}
