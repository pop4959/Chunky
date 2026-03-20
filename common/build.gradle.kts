plugins {
    id("common")
}

dependencies {
    compileOnly(libs.gson)
    testImplementation(libs.junit)
    testImplementation(libs.gson)
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
