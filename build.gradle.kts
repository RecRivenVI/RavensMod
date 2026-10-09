plugins {
    base
    id("raven.repository-configuration")
}

tasks.check {
    dependsOn(gradle.includedBuild("raven-configuration").task(":check"))
}

tasks.named("build") {
    dependsOn(":version:1.21.4-fabric:build")
}

tasks.named("clean") {
    dependsOn(":version:1.21.4-fabric:clean")
}
