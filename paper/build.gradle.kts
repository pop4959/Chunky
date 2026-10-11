repositories {
    maven("https://maven-prs.papermc.io/Paper/pr14373") {
        name = "Maven for PR #14373" // https://github.com/PaperMC/Paper/pull/14373
        mavenContent {
            includeModule("io.papermc.paper", "paper-api")
        }
    }
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(group = "io.papermc.paper", name = "paper-api", version = "26.3.local-SNAPSHOT")
}
