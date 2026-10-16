repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(group = "io.papermc.paper", name = "paper-api", version = "26.3.build.186-stable")
    implementation(project(":chunky-common"))
}
