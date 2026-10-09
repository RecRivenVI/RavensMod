import raven.gradle.RepositoryConfiguration
import raven.gradle.LoomRunAdapter

plugins {
    java
    id("net.fabricmc.fabric-loom-remap") version "1.17.19"
}

val config = rootProject.extensions.getByType<RepositoryConfiguration>()
val target = project.name
val facts = config.projectFacts
val targetFacts = config.targetFacts(target)
val minecraftVersion = targetFacts.getValue("minecraft_version")
val fabricLoaderVersion = targetFacts.getValue("loader_version")
val fabricApiVersion = targetFacts.getValue("fabric_api_version")
val javaVersion = config.javaVersion(target)

version = "${facts.getValue("mod_version")}+$minecraftVersion"
base.archivesName.set(facts.getValue("mod_artifact_name"))

repositories {
    mavenCentral()
}

val loom = the<net.fabricmc.loom.api.LoomGradleExtensionAPI>()

loom.splitEnvironmentSourceSets()
loom.mods.create(facts.getValue("mod_id")) {
    sourceSet(sourceSets.getByName("main"))
    sourceSet(sourceSets.getByName("client"))
}

val clientRun = loom.runs.getByName("client")

loom.runs.create("clientMultiplayer").apply {
    inherit(clientRun)
    displayName.set("Minecraft Client Multiplayer")
    generateRunConfig.set(true)
}

mapOf("client" to "client", "clientMultiplayer" to "client-multiplayer", "server" to "server").forEach { (runName, variant) ->
    loom.runs.getByName(runName).runDirectory.set(rootProject.layout.projectDirectory.dir("instances/$target/$variant"))
    tasks.named<JavaExec>("run" + runName.replaceFirstChar(Char::uppercaseChar)) {
        LoomRunAdapter.configure(this, config, target, variant)
    }
}

dependencies {
    "minecraft"("com.mojang:minecraft:$minecraftVersion")
    "mappings"(loom.officialMojangMappings())
    "modImplementation"("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    "modImplementation"("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
}

tasks.processResources {
    val properties = mapOf(
        "mod_id" to facts.getValue("mod_id"),
        "mod_name" to facts.getValue("mod_name"),
        "mod_author" to facts.getValue("mod_author"),
        "mod_homepage" to facts.getValue("mod_homepage"),
        "mod_sources" to facts.getValue("mod_sources"),
        "mod_environment" to if (config.environment == "both") "*" else config.environment,
        "java_version" to javaVersion.toString(),
        "version" to project.version.toString(),
        "mc_version" to minecraftVersion,
        "loader_version" to fabricLoaderVersion,
    )
    inputs.properties(properties)
    filesMatching("fabric.mod.json") {
        expand(properties)
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(javaVersion)
}
