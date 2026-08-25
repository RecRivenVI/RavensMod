plugins {
    base
}

tasks.named("build") {
    dependsOn(":versions:1.21.4-fabric:build")
}

tasks.named("clean") {
    dependsOn(":versions:1.21.4-fabric:clean")
}
