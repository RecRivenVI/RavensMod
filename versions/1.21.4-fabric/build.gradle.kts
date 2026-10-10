plugins {
    id("net.fabricmc.fabric-loom-remap")
}

dependencies {
    modImplementation(target.module("net.fabricmc.fabric-api:fabric-api", "fabric_api_version"))
}

java {
    withSourcesJar()
}
