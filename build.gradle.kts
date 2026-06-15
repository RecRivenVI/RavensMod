buildscript {
    repositories {
        maven("https://maven.fabricmc.net/")
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("net.fabricmc:fabric-loom:1.17.11")
    }
}

plugins {
    id("dev.kikugie.stonecutter")
}

if (sc.current.parsed >= "26.1") {
    apply(plugin = "net.fabricmc.fabric-loom")
} else {
    apply(plugin = "net.fabricmc.fabric-loom-remap")
}

version = "${property("mod_version")}${sc.current.version}"
base.archivesName.set(property("mod_id") as String)

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    else -> JavaVersion.VERSION_17
}

repositories {
    exclusiveContent {
        forRepository {
            maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/") {
                name = "GeckoLib"
            }
        }
        filter {
            includeGroupByRegex("com\\.geckolib.*")
            includeGroupByRegex("software\\.bernie\\.geckolib.*")
        }
    }
}

val loom = the<net.fabricmc.loom.api.LoomGradleExtensionAPI>()

loom.splitEnvironmentSourceSets()

loom.mods.create("ravensmod") {
    sourceSet(sourceSets.getByName("main"))
    sourceSet(sourceSets.getByName("client"))
}

dependencies {
    "minecraft"("com.mojang:minecraft:${sc.current.version}")
    if (sc.current.parsed < "26.1") {
        "mappings"(loom.officialMojangMappings())
    }

    val loaderVersion = property("loader_version") as String
    val fabricApiVersion = property("fabric_api_version") as String
    val geckolibVersion = property("geckolib_version") as String
    val geckolibGroup = property("geckolib_group") as String
    val geckolibArtifact = property("geckolib_artifact") as String

    if (sc.current.parsed >= "26.1") {
        "implementation"("net.fabricmc:fabric-loader:$loaderVersion")
        "implementation"("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
        "implementation"("$geckolibGroup:$geckolibArtifact:$geckolibVersion")
    } else {
        "modImplementation"("net.fabricmc:fabric-loader:$loaderVersion")
        "modImplementation"("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
        "modImplementation"("$geckolibGroup:$geckolibArtifact:$geckolibVersion")
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("mc_version", sc.current.version)

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version.toString(),
            "mc_version" to sc.current.version,
            "loader_version" to project.property("loader_version") as String
        )
    }
}

java {
    withSourcesJar()
    sourceCompatibility = requiredJava
    targetCompatibility = requiredJava
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(requiredJava.majorVersion))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(requiredJava.majorVersion.toInt())
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.name}" }
    }
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.jar)
    into(rootProject.layout.buildDirectory.file("libs/${property("mod_version")}"))
    dependsOn("build")
}
