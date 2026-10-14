plugins {
    id("net.neoforged.moddev")
}

java {
    withSourcesJar()
}

repositories {
    maven("https://maven.createmod.net")
}

dependencies {
    compileOnly(
        target.module(
            "dev.engine-room.flywheel:flywheel-neoforge-${target.fact("minecraft_version")}",
            "flywheel_version",
        ),
    )
}

val shaderPacks =
    files(target.input("iterationrp"), target.input("sundial"), target.input("sundial_core"))
val prepareShaderPacks =
    tasks.register<Copy>("prepareShaderPacks") {
        dependsOn(tasks.named("verifyInputs"))
        from(shaderPacks)
        into(rootProject.layout.projectDirectory.dir("instances/${target.name}/client/shaderpacks"))
    }

tasks.configureEach {
    if (name == "runClient") {
        dependsOn(prepareShaderPacks)
    }
}
