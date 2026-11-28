repositories {
    maven("https://libraries.minecraft.net")
}

dependencies {
    compileOnly(libs.gson)
    compileOnly(libs.brigadier)
    testImplementation(libs.junit)
    testImplementation(libs.gson)
    implementation(project(":chunky-nbt"))
}

tasks {
    javadoc {
        setDestinationDir(rootProject.projectDir.resolve("docs/chunky/javadoc"))
        include("org/popcraft/chunky/api/**")
        exclude("org/popcraft/chunky/api/ChunkyAPIImpl.java")
    }
}
