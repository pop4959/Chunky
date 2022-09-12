dependencies {
    compileOnly(group = "com.google.code.gson", name = "gson", version = "2.8.9")
    testImplementation(group = "junit", name = "junit", version = "4.13.2")
    testImplementation(group = "com.google.code.gson", name = "gson", version = "2.8.9")
}

tasks {
    processResources {
        filesMatching("version.properties") {
            expand(
                "version" to project.version
            )
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
