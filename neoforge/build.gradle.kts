plugins {
    id("modded")
}

repositories {
    maven("https://maven.neoforged.net/releases/")
}

dependencies {
    neoForge(libs.neoforge)
}

chunky {
    name = "NeoForge"
}

modded {
    processMatching = "META-INF/neoforge.mods.toml"
    forge = true
}

tasks {
    remapJar {
        inputFile.set(shadowJar.get().archiveFile)
    }
}
