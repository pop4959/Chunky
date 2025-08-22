repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(group = "io.papermc.paper", name = "paper-api", version = "1.21.1-R0.1-SNAPSHOT")
    implementation(project(":chunky-common"))
}
