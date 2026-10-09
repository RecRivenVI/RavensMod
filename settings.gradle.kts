pluginManagement {
    includeBuild("gradle/configuration") {
        name = "raven-configuration"
    }
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "RavensModels"

include(":version:1.21.4-fabric")
project(":version").projectDir = file("versions")
project(":version:1.21.4-fabric").projectDir = file("versions/1.21.4-fabric")
