dependencies {
    compileOnly("com.google.code.gson:gson:2.8.9")
    compileOnly("com.mojang:brigadier:1.0.18")
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.code.gson:gson:2.10.1")
    implementation(project(":chunky-nbt"))
}

tasks {
    processResources {
        val properties = mapOf(
            "version" to project.version
        )
        inputs.properties(properties)
        filesMatching("version.properties") {
            expand(properties)
        }
    }
    javadoc {
        sourceSets {
            main {
                allJava
            }
        }
        setDestinationDir(rootProject.projectDir.resolve("docs/chunky/javadoc"))
        include("org/popcraft/chunky/api/**")
        exclude("org/popcraft/chunky/api/ChunkyAPIImpl.java")
    }
}
