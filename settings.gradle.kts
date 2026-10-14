pluginManagement {
    includeBuild("gradle/conventions")
    repositories {
        gradlePluginPortal()
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.fabricmc.net/")
    }
}

plugins { id("io.github.recrivenvi.conventions") }

rootProject.name = "RavensModels"

targets {
    register("1.21.4-fabric")
    register("1.21.1-neoforge")
}
