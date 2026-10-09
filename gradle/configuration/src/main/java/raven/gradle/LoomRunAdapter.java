package raven.gradle;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Task;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.JavaExec;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.process.CommandLineArgumentProvider;

/** RavensModels' Loom 1.17 adapter. RunConfiguration owns directory/source set;
 * AbstractRunTask retains its native argument providers and exec implementation. */
public final class LoomRunAdapter {
    private LoomRunAdapter() {}

    public static void configure(JavaExec task, RepositoryConfiguration config, String target, String variant) {
        config.targetFacts(target);
        if (task.getExtensions().getExtraProperties().has("ravensmodels.instanceConfigured")) {
            throw new GradleException("Loom instance already configured: " + task.getPath());
        }
        task.getExtensions().getExtraProperties().set("ravensmodels.instanceConfigured", true);
        File executable = config.javaExecutable(config.javaVersion(target));
        if (executable != null) task.setExecutable(executable);
        else task.getJavaLauncher().set(task.getProject().getExtensions().getByType(JavaToolchainService.class)
                .launcherFor(spec -> spec.getLanguageVersion().set(JavaLanguageVersion.of(config.javaVersion(target)))));
        // Appending providers, not JavaExec.args(), keeps Loom's arguments first.
        task.getArgumentProviders().add(new InstanceArguments(config, target, variant, false));
        task.getJvmArgumentProviders().add(new InstanceArguments(config, target, variant, true));
        task.doFirst(new Prepare(config, target, variant));
    }

    static RepositoryConfiguration.ResolvedInstance settings(RepositoryConfiguration config, String target, String variant) {
        var settings = config.resolveInstance(target, variant);
        if (!variant.equals("server") && settings.playerName() == null) {
            throw new GradleException("Missing instance." + variant + ".playerName");
        }
        if (variant.equals("client-multiplayer")) {
            var first = config.value(target, "client", "playerName");
            if (first == null || !first.raw().trim().matches("[A-Za-z0-9_]{3,16}")) {
                throw new GradleException("Client playerName must contain 3-16 ASCII letters, digits or underscores");
            }
            if (first.raw().trim().equals(settings.playerName())) {
                throw new GradleException("Client and client-multiplayer playerName must be present and different");
            }
        }
        return settings;
    }

    public static final class InstanceArguments implements CommandLineArgumentProvider, Serializable {
        private final RepositoryConfiguration config;
        private final String target;
        private final String variant;
        private final boolean jvm;

        public InstanceArguments(RepositoryConfiguration config, String target, String variant, boolean jvm) {
            this.config = config;
            this.target = target;
            this.variant = variant;
            this.jvm = jvm;
        }

        @Internal public RepositoryConfiguration getConfiguration() { return config; }

        @Override public Iterable<String> asArguments() {
            var settings = settings(config, target, variant);
            if (jvm) return settings.jvmArgumentsWithoutMaxHeap();
            var args = new java.util.ArrayList<>(settings.clientArguments());
            args.addAll(settings.extraGameArgs());
            return List.copyOf(args);
        }
    }

    public record Prepare(RepositoryConfiguration config, String target, String variant) implements Action<Task>, Serializable {
        @Override public void execute(Task task) {
            var settings = settings(config, target, variant);
            config.javaExecutable(config.javaVersion(target));
            if (settings.effectiveMemory() != null) ((JavaExec) task).setMaxHeapSize(settings.effectiveMemory());
            // Loom AbstractRunTask.exec() applies RunConfiguration.runDirectory and creates it.
        }
    }
}
