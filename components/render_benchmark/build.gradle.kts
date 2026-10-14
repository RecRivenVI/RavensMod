plugins { java }

dependencies { implementation(libs.tomlj) }

val testReportParser =
    tasks.register<JavaExec>("testReportParser") {
        classpath = sourceSets.main.get().runtimeClasspath
        mainClass = "io.github.recrivenvi.ravensmodels.benchmark.VerifyBenchmark"
        args("--self-test")
    }

tasks.named("check") { dependsOn(testReportParser) }

tasks.register<JavaExec>("verifyBenchmark") {
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "io.github.recrivenvi.ravensmodels.benchmark.VerifyBenchmark"
    args(
        layout.projectDirectory
            .file("../../validations/performance-chunk_rebuild/result/benchmark.properties")
            .asFile
            .absolutePath,
    )
}

tasks.register<JavaExec>("prepareFallback") {
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "io.github.recrivenvi.ravensmodels.benchmark.FallbackTools"
    args("prepare", layout.projectDirectory.dir("../..").asFile.absolutePath)
}

tasks.register<JavaExec>("verifyFallback") {
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "io.github.recrivenvi.ravensmodels.benchmark.FallbackTools"
    args("verify", layout.projectDirectory.dir("../..").asFile.absolutePath)
}
