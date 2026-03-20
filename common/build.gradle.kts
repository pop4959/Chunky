plugins {
    id("common")
}

dependencies {
    compileOnly("com.google.code.gson:gson:2.8.9")
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.code.gson:gson:2.10.1")
    implementation(project(":chunky-nbt"))
}

tasks {
    processResources {
        val props = mapOf("version" to project.version)
        filesMatching("version.properties") {
            expand(props)
        }
    }
    javadoc {
        sourceSets {
            main {
                allJava
            }
        }
        destinationDir = rootProject.projectDir.resolve("docs/chunky/javadoc")
        include("org/popcraft/chunky/api/**")
        exclude("org/popcraft/chunky/api/ChunkyAPIImpl.java")
    }
}
