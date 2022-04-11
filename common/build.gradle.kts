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
}
