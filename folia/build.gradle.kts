plugins {
    id("common")
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("dev.folia:folia-api:1.20.2-R0.1-SNAPSHOT")
}
