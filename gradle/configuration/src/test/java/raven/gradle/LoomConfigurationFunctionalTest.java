package raven.gradle;

import static org.junit.jupiter.api.Assertions.*;
import groovy.json.JsonSlurper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.tools.ToolProvider;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

/** Actual Loom configuration/argument providers, never AbstractRunTask.exec or Minecraft. */
class LoomConfigurationFunctionalTest {
    @TempDir(cleanup = CleanupMode.NEVER) Path root;
    static final String TARGET = "1.21.4-fabric";

    private void write(String file, String value) throws IOException {
        Path path = root.resolve(file);
        Files.createDirectories(path.getParent());
        Files.writeString(path, value);
    }

    private void fixture() throws IOException {
        Path project = Path.of(System.getProperty("template.root"));
        String settings = Files.readString(project.resolve("settings.gradle.kts"))
                .replaceAll("(?s)\\s*includeBuild\\(\"gradle/configuration\"\\)\\s*\\{[^}]*}", "");
        write("settings.gradle.kts", settings);
        write("build.gradle.kts", "plugins { base; id(\"raven.repository-configuration\") }");
        for (String file : List.of("gradle.properties", "instance.properties", "versions/" + TARGET + "/target.properties", "versions/" + TARGET + "/build.gradle.kts")) {
            write(file, Files.readString(project.resolve(file)));
        }
        write("inspect.gradle", """
                import groovy.json.JsonOutput
                import org.gradle.process.ExecOperations
                import javax.inject.Inject
                abstract class ProbeExec {
                    @Inject abstract ExecOperations getExecOperations()
                }
                gradle.projectsEvaluated {
                    def target = rootProject.project(':version:1.21.4-fabric')
                    target.tasks.register('inspectLoom') {
                        doLast {
                            def variant = rootProject.findProperty('probeVariant') ?: 'client'
                            def runName = variant == 'client-multiplayer' ? 'clientMultiplayer' : variant
                            def run = target.tasks.getByName('run' + runName.capitalize())
                            assert run.extensions.extraProperties.get('ravensmodels.instanceConfigured')
                            // Only our doFirst preparation, never Loom's exec TaskAction.
                            assert run.actions.size() > 1
                            run.actions.first().execute(run)
                            def nativeRun = target.extensions.getByName('loom').runs.getByName(runName)
                            def args = (run.args ?: []) + run.argumentProviders.collectMany { it.asArguments().toList() }
                            def jvm = run.allJvmArgs
                            def data = [variant:variant, args:args, jvm:jvm, heap:run.maxHeapSize,
                                directory:run.internalRunDir.get().asFile.absolutePath,
                                nativeDirectory:nativeRun.runDirectory.get().asFile.absolutePath,
                                sourceSet:nativeRun.sourceSet.get(), executable:run.executable,
                                mainClass:run.mainClass.get(), providerCount:run.argumentProviders.size()]
                            rootProject.file('loom-report.json').text = JsonOutput.toJson(data)
                            assert !new File(data.directory).exists()
                            if (rootProject.hasProperty('launchProbe')) {
                                new File(data.directory).mkdirs()
                                // A tiny Java process receives the inspected Loom argument list;
                                // this is not a Loader or Minecraft launch.
                                rootProject.objects.newInstance(ProbeExec).execOperations.javaexec {
                                    executable = data.executable
                                    classpath(rootProject.file('probe-classes'))
                                    mainClass.set('ArgumentProbe')
                                    workingDir(data.directory)
                                    jvmArgs(data.jvm)
                                    setArgs(data.args)
                                }
                            }
                        }
                    }
                }
                """);
    }

    private GradleRunner runner(String variant, String... extra) {
        var args = new ArrayList<>(List.of(":version:" + TARGET + ":inspectLoom", "-I", "inspect.gradle", "-PprobeVariant=" + variant, "--stacktrace", "--max-workers=1", "--console=plain"));
        args.addAll(List.of(extra));
        return GradleRunner.create().withProjectDir(root.toFile()).withPluginClasspath()
                .withGradleInstallation(new File(System.getProperty("gradle.installation"))).withArguments(args);
    }

    @SuppressWarnings("unchecked") private Map<String, Object> report() throws IOException {
        return (Map<String, Object>) new JsonSlurper().parseText(Files.readString(root.resolve("loom-report.json")));
    }

    @Test void actualLoomProfilesKeepDefaultsNativeArgumentsAndIsolation() throws IOException {
        fixture();
        for (String variant : List.of("client", "client-multiplayer", "server")) {
            runner(variant).build();
            var data = report();
            assertEquals(root.resolve("instances/" + TARGET + "/" + variant).toString(), data.get("directory"));
            assertEquals(data.get("directory"), data.get("nativeDirectory"));
            assertTrue(data.get("mainClass").toString().contains("devlaunchinjector"));
            assertTrue(data.get("jvm").toString().contains("fabric.dli"));
            assertTrue(((Number) data.get("providerCount")).intValue() >= 3);
            assertEquals(variant.equals("server") ? "main" : "client", data.get("sourceSet"));
            var args = (List<?>) data.get("args");
            if (variant.equals("server")) {
                assertTrue(args.contains("nogui"));
                assertFalse(args.contains("--username"));
            } else {
                assertEquals(1, args.stream().filter("--username"::equals).count());
                assertTrue(args.contains(variant.equals("client") ? "DevPlayer" : "DevPlayer2"));
            }
        }
        assertFalse(Files.exists(root.resolve("instances")));
        assertFalse(Files.exists(root.resolve("local.properties")));
    }

    @Test void localCascadeArgumentsAndJdkReachAnIsolatedJavaProcess() throws IOException {
        fixture();
        Files.writeString(root.resolve("instance.properties"), "\ninstance.[1.21.4-fabric].client.memory=192M\ninstance.extraJvmArgs=[\n-Xmx128M\n-Dmessage=hello world\n]\n", java.nio.file.StandardOpenOption.APPEND);
        write("local.properties", "java.21.home=" + System.getProperty("java.home").replace('\\', '/') + "\ninstance.memory=64M\ninstance.client.width=900\ninstance.client.height=600\ninstance.extraGameArgs=[\"--extra\",\"two words\",\"\",\"雪\"]\n");
        write("ArgumentProbe.java", """
                public class ArgumentProbe {
                    public static void main(String[] args) {
                        System.out.println("probe.heap=" + Runtime.getRuntime().maxMemory());
                        System.out.println("probe.xmxCount=" + java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().stream().filter(a -> a.startsWith("-Xmx")).count());
                        System.out.println("probe.message=" + System.getProperty("message"));
                        System.out.println("probe.major=" + Runtime.version().feature());
                        System.out.println("probe.cwd=" + System.getProperty("user.dir"));
                        System.out.println("probe.args64=" + java.util.Base64.getEncoder().encodeToString(String.join("|",args).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                    }
                }
                """);
        Files.createDirectories(root.resolve("probe-classes"));
        assertEquals(0, ToolProvider.getSystemJavaCompiler().run(null, null, null, "--release", "17", "-d", root.resolve("probe-classes").toString(), root.resolve("ArgumentProbe.java").toString()));
        String out = runner("client", "-PlaunchProbe").build().getOutput();
        assertTrue(out.contains("probe.heap=134217728"), out);
        assertTrue(out.contains("probe.xmxCount=1"), out);
        assertTrue(out.contains("probe.message=hello world"), out);
        assertTrue(out.contains("probe.major=21"), out);
        assertTrue(out.contains("probe.cwd=" + root.resolve("instances/" + TARGET + "/client")), out);
        String expected = "--width|900|--height|600|--username|DevPlayer|--extra|two words||雪";
        assertTrue(out.contains("probe.args64=" + java.util.Base64.getEncoder().encodeToString(expected.getBytes(java.nio.charset.StandardCharsets.UTF_8))), out);
        assertEquals("128M", report().get("heap"));
    }

    @Test void negativeClientValuesDoNotBlockServerOrTargetBuild() throws IOException {
        fixture();
        write("local.properties", "instance.client.playerName=bad name\ninstance.width=bad\n");
        String badClient = runner("client").buildAndFail().getOutput();
        assertTrue(badClient.contains("instance.width"), badClient);
        runner("server").build();
        String rootFailure = GradleRunner.create().withProjectDir(root.toFile()).withPluginClasspath()
                .withGradleInstallation(new File(System.getProperty("gradle.installation")))
                .withArguments("check", "--max-workers=1").buildAndFail().getOutput();
        assertTrue(rootFailure.contains("instance.client.playerName"), rootFailure);
        GradleRunner.create().withProjectDir(root.toFile()).withPluginClasspath()
                .withGradleInstallation(new File(System.getProperty("gradle.installation")))
                .withArguments(":version:" + TARGET + ":build", ":version:" + TARGET + ":test", "--max-workers=1").build();
        write("local.properties", "instance.playerName=SamePlayer\n");
        assertTrue(runner("client-multiplayer").buildAndFail().getOutput().contains("must be present and different"));
        write("local.properties", "instance.client.playerName=DevPlayer\ninstance.extraGameArgs=[\"--gameDir\",\"elsewhere\"]\n");
        assertTrue(runner("client").buildAndFail().getOutput().contains("dedicated settings"));
        assertFalse(Files.exists(root.resolve("instances")));
    }
}
