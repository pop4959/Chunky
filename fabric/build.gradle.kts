plugins {
    id("modded")
}

dependencies {
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    modCompileOnly(libs.fabric.permissions.api)
}

modded {
    processMatching = "fabric.mod.json"
}

tasks {
    remapJar {
        inputFile.set(shadowJar.get().archiveFile)
    }
}
