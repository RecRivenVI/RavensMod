package io.github.recrivenvi.ravensmodels.benchmark;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Properties;
import org.tomlj.Toml;

public final class FallbackTools {
    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[1]).normalize();
        Path validation = root.resolve("validations/performance-render_fallback");
        var config = Toml.parse(validation.resolve("validation.toml"));
        if (args[0].equals("prepare")) {
            var local = Toml.parse(root.resolve("local.toml"));
            var inputs = Toml.parse(root.resolve("inputs.toml"));
            String name = config.getString("benchmark.shader");
            Path shader = Path.of(local.getString("inputs." + name));
            String actual;
            try (var input = Files.newInputStream(shader)) {
                var digest = MessageDigest.getInstance("SHA-256");
                byte[] buffer = new byte[65536];
                int length;
                while ((length = input.read(buffer)) != -1) digest.update(buffer, 0, length);
                actual = HexFormat.of().formatHex(digest.digest());
            }
            if (!inputs.getArray(name + ".sha256").toList().contains(actual))
                throw new IllegalStateException("Shader hash mismatch");
            Path instance =
                    validation.resolve(
                            "instance/" + config.getArray("targets").getString(0) + "/client");
            Files.createDirectories(instance.resolve("shaderpacks"));
            Files.createDirectories(instance.resolve("config"));
            Files.copy(
                    shader,
                    instance.resolve("shaderpacks").resolve(shader.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING);
            Properties iris = new Properties();
            iris.setProperty("enableShaders", "true");
            iris.setProperty("shaderPack", shader.getFileName().toString());
            try (var output = Files.newBufferedWriter(instance.resolve("config/iris.properties"))) {
                iris.store(output, "Fallback benchmark shader");
            }
            System.out.println("Prepared verified shader: " + shader.getFileName());
        } else {
            Properties result = new Properties();
            try (var reader =
                    Files.newBufferedReader(validation.resolve("result/fallback.properties"))) {
                result.load(reader);
            }
            if (!result.getProperty("passed", "false").equals("true")
                    || !result.getProperty("flywheel_installed", "true").equals("false"))
                throw new IllegalStateException("Fallback benchmark failed: " + result);
            Properties sceneSettings = new Properties();
            try (var reader =
                    Files.newBufferedReader(validation.resolve("task/scene.properties"))) {
                sceneSettings.load(reader);
            }
            for (String scene : sceneSettings.getProperty("copies").split(",")) {
                for (String mode : new String[] {"baked", "ber"}) {
                    String prefix = scene + "." + mode + ".";
                    if (Double.parseDouble(result.getProperty(prefix + "cpu_median_ms", "0")) <= 0
                            || Double.parseDouble(result.getProperty(prefix + "gpu_median_ms", "0"))
                                    <= 0)
                        throw new IllegalStateException("Missing timing: " + prefix);
                }
                if (!result.getProperty(scene + ".ber.terrain_quads", "missing").equals("0"))
                    throw new IllegalStateException("BER terrain duplication");
            }
            System.out.println("Fallback benchmark PASS: " + result);
        }
    }
}
