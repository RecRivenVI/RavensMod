import java.util.Properties

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
    }
    dependencies {
        classpath("net.fabricmc:fabric-loom:1.17.11")
    }
}

plugins {
    java
}

apply(plugin = "net.fabricmc.fabric-loom-remap")

val minecraftVersion = "1.21.4"
val fabricLoaderVersion = "0.16.14"
val fabricApiVersion = "0.119.4+1.21.4"
val geckolibVersion = "4.8.5"

group = rootProject.property("maven_group") as String
version = "${rootProject.property("mod_version")}+$minecraftVersion"
base.archivesName.set(rootProject.property("artifact_name") as String)

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository {
            maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/") { name = "GeckoLib" }
        }
        filter {
            includeGroup("software.bernie.geckolib")
        }
    }
}

val loom = the<net.fabricmc.loom.api.LoomGradleExtensionAPI>()

loom.splitEnvironmentSourceSets()
loom.mods.create(rootProject.property("mod_id") as String) {
    sourceSet(sourceSets.getByName("main"))
    sourceSet(sourceSets.getByName("client"))
}

val defaultClientPlayerName = "DevPlayer"
val defaultMultiplayerPlayerName = "DevPlayer2"
val localProperties = Properties()
rootProject.file("local.properties").takeIf(File::isFile)?.inputStream()?.use(localProperties::load)

val clientPlayerName = localProperties.getProperty("player.client.name", defaultClientPlayerName)
val multiplayerPlayerName = localProperties.getProperty("player.client-multiplayer.name", defaultMultiplayerPlayerName)
val playerNamePattern = Regex("[A-Za-z0-9_]{3,16}")

fun validatePlayerName(propertyName: String, playerName: String) {
    require(playerNamePattern.matches(playerName)) {
        "$propertyName must contain 3-16 characters using only A-Z, a-z, 0-9, or _."
    }
}

val clientRun = loom.runs.getByName("client")
clientRun.runDirectory.set(rootProject.layout.projectDirectory.dir("runs/1.21.4-fabric/client"))

loom.runs.create("clientMultiplayer").apply {
    inherit(clientRun)
    displayName.set("Minecraft Client Multiplayer")
    runDirectory.set(rootProject.layout.projectDirectory.dir("runs/1.21.4-fabric/client-multiplayer"))
    generateRunConfig.set(true)
}

loom.runs.getByName("server").runDirectory.set(rootProject.layout.projectDirectory.dir("runs/1.21.4-fabric/server"))

tasks.named<JavaExec>("runClient") {
    args("--username", clientPlayerName)
    doFirst {
        validatePlayerName("player.client.name", clientPlayerName)
    }
}

tasks.named<JavaExec>("runClientMultiplayer") {
    args("--username", multiplayerPlayerName)
    doFirst {
        validatePlayerName("player.client.name", clientPlayerName)
        validatePlayerName("player.client-multiplayer.name", multiplayerPlayerName)
        require(clientPlayerName != multiplayerPlayerName) {
            "player.client.name and player.client-multiplayer.name must be different."
        }
    }
}

dependencies {
    "minecraft"("com.mojang:minecraft:$minecraftVersion")
    "mappings"(loom.officialMojangMappings())
    "modImplementation"("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    "modImplementation"("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    "modImplementation"("software.bernie.geckolib:geckolib-fabric-1.21.4:$geckolibVersion")
}

tasks.processResources {
    val properties = mapOf(
        "mod_id" to rootProject.property("mod_id") as String,
        "mod_name" to rootProject.property("mod_name") as String,
        "mod_author" to rootProject.property("mod_author") as String,
        "mod_license" to rootProject.property("mod_license") as String,
        "mod_description" to rootProject.property("mod_description") as String,
        "mod_homepage" to rootProject.property("mod_homepage") as String,
        "mod_sources" to rootProject.property("mod_sources") as String,
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
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

tasks.named("clientClasses") {
    doLast {
        layout.buildDirectory.dir("resources/client").get().asFile.mkdirs()
    }
}

tasks.jar {
    from(rootProject.file("LICENSE")) {
        rename { "${it}_${project.name}" }
    }
}
