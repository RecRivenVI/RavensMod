plugins {
    id("net.fabricmc.fabric-loom") version "1.17.11"
}

version = "${property("mod_version")}${sc.current.version}"
base.archivesName = property("mod_id") as String

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

loom {
    splitEnvironmentSourceSets()

    mods {
        "ravensmod" {
            sourceSet(sourceSets.main)
            sourceSet(sourceSets.client)
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    loom.applyMappings()

    val loaderVersion: String by project
    val fabricApiVersion: String by project

    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")

    val geckolibVersion: String by project
    val geckolibGroup: String by project
    val geckolibArtifact: String by project
    modImplementation("$geckolibGroup:$geckolibArtifact:$geckolibVersion")
}

processResources {
    inputs.property("version", project.version)
    inputs.property("mc_version", sc.current.version)

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version.toString(),
            "mc_version" to sc.current.version,
            "loader_version" to (property("loader_version") as String)
        )
    }
}

java {
    withSourcesJar()
    sourceCompatibility = requiredJava
    targetCompatibility = requiredJava
    toolchain {
        vendor = JvmVendorSpec.BELLSOFT
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = requiredJava.majorVersion.toInt()
}

jar {
    from("LICENSE") {
        rename { "${it}_${project.name}" }
    }
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.named("jar"))
    into(rootProject.layout.buildDirectory.file("libs/${property("mod_version")}"))
    dependsOn("build")
}
