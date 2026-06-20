buildscript {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases")
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("net.fabricmc:fabric-loom:1.17.11")
        classpath("net.neoforged:moddev-gradle:2.0.141")
    }
}

plugins {
    id("dev.kikugie.stonecutter")
}

val isFabric = sc.current.project.startsWith("fabric")
val isNeoForge = sc.current.project.startsWith("neoforge")

sc.constants {
    put("fabric", isFabric)
    put("neoforge", isNeoForge)
}

if (isFabric) {
    if (sc.current.parsed >= "26.1") {
        apply(plugin = "net.fabricmc.fabric-loom")
    } else {
        apply(plugin = "net.fabricmc.fabric-loom-remap")
    }
} else {
    apply(plugin = "net.neoforged.moddev")
}

version = "${property("mod_version")}+${sc.current.version}"
base.archivesName.set(property("mod_id") as String)

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    else -> JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    maven("https://maven.architectury.dev/") { name = "Architectury" }
    exclusiveContent {
        forRepository {
            maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/") { name = "GeckoLib" }
        }
        filter {
            includeGroupByRegex("com\\.geckolib.*")
            includeGroupByRegex("software\\.bernie\\.geckolib.*")
        }
    }
}

val geckolibVersion = property("geckolib_version") as String
val geckolibGroup = property("geckolib_group") as String
val geckolibArtifact = property("geckolib_artifact") as String
val architecturyVersion = property("architectury_version") as String

if (isFabric) {
    val loom = the<net.fabricmc.loom.api.LoomGradleExtensionAPI>()
    loom.splitEnvironmentSourceSets()
    loom.mods.create("ravensmod") {
        sourceSet(sourceSets.getByName("main"))
        sourceSet(sourceSets.getByName("client"))
    }

    sourceSets {
        named("main") {
            java.srcDir("src/fabric/java")
            resources.srcDir("src/fabric/resources")
            java.exclude("**/neoforge/**")
        }
        named("client") {
            java.exclude("**/neoforge/**")
        }
    }

    dependencies {
        "minecraft"("com.mojang:minecraft:${sc.current.version}")
        if (sc.current.parsed < "26.1") {
            "mappings"(loom.officialMojangMappings())
        }
        val loaderVersion = property("loader_version") as String
        val fabricApiVersion = property("fabric_api_version") as String
        if (sc.current.parsed >= "26.1") {
            "implementation"("net.fabricmc:fabric-loader:$loaderVersion")
            "implementation"("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
            "implementation"("dev.architectury:architectury-fabric:$architecturyVersion")
            "implementation"("$geckolibGroup:$geckolibArtifact:$geckolibVersion")
        } else {
            "modImplementation"("net.fabricmc:fabric-loader:$loaderVersion")
            "modImplementation"("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
            "modImplementation"("dev.architectury:architectury-fabric:$architecturyVersion")
            "modImplementation"("$geckolibGroup:$geckolibArtifact:$geckolibVersion")
        }
    }

    tasks.processResources {
        inputs.property("version", project.version)
        inputs.property("mc_version", sc.current.version)
        exclude("META-INF/neoforge.mods.toml")
        filesMatching("fabric.mod.json") {
            expand(
                "version" to project.version.toString(),
                "mc_version" to sc.current.version,
                "loader_version" to project.property("loader_version") as String
            )
        }
    }
} else {
    val neoforgeVersion = property("neoforge_version") as String

    sourceSets {
        named("main") {
            java.srcDir("src/neoforge/java")
            resources.srcDir("src/neoforge/resources")
            java.exclude("**/fabric/**")
        }
        val client by registering {
            java.srcDir("src/client/java")
            resources.srcDir("src/client/resources")
            compileClasspath += sourceSets.main.get().output + sourceSets.main.get().compileClasspath
            runtimeClasspath += sourceSets.main.get().output + sourceSets.main.get().runtimeClasspath
            java.exclude("**/fabric/**")
        }
    }

    extensions.configure<net.neoforged.moddevgradle.dsl.NeoForgeExtension>("neoForge") {
        version = neoforgeVersion
        runs {
            register("client") {
                client()
            }
        }
        mods {
            register("ravensmod") {
                sourceSet(sourceSets.main.get())
                sourceSet(sourceSets.named("client").get())
            }
        }
    }

    dependencies {
        "implementation"("dev.architectury:architectury-neoforge:$architecturyVersion")
        "implementation"("$geckolibGroup:$geckolibArtifact:$geckolibVersion")
    }

    tasks.processResources {
        inputs.property("version", project.version)
        inputs.property("mc_version", sc.current.version)
        exclude("fabric.mod.json")
        exclude("ravensmod.mixins.json")
        exclude("ravensmod.client.mixins.json")
        filesMatching("META-INF/neoforge.mods.toml") {
            expand(
                "version" to project.version.toString(),
                "mc_version" to sc.current.version
            )
        }
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
    into(rootProject.layout.buildDirectory.dir("libs"))
    dependsOn("build")
}
