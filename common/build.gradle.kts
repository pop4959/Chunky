repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.mikeprimm.com")
    maven("https://repo.pl3x.net")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly(group = "us.dynmap", name = "dynmap-api", version = "3.1")
    compileOnly(group = "com.github.BlueMap-Minecraft", name = "BlueMapAPI", version = "v1.6.0")
    compileOnly(group = "net.pl3x.map", name = "pl3xmap-api", version = "1.0.0-SNAPSHOT")
    compileOnly(group = "com.github.Brettflan", name = "WorldBorder", version = "c0d1772418")
    testImplementation(group = "junit", name = "junit", version = "4.13.2")
    testImplementation(group = "com.google.code.gson", name = "gson", version = "2.8.7")
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
