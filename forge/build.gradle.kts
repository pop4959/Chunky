plugins {
    id("modded")
}

dependencies {
    forge(libs.forge)
}

modded {
    processMatching = "META-INF/mods.toml"
    forge = true
}

loom {
    forge {
        mixinConfig("chunky.mixins.json")
    }
}

tasks {
    remapJar {
        enabled = false
    }
}
