package io.github.recrivenvi.ravensmodels.benchmark;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class VerifyBenchmark {
    public static void main(String[] args) throws Exception {
        if (args.length == 1 && args[0].equals("--self-test")) {
            Properties p = new Properties();
            p.setProperty("passed", "false");
            try {
                verify(p);
                throw new AssertionError("Accepted a failed result");
            } catch (IllegalStateException expected) {
                System.out.println("Result rejection self-test PASS");
            }
            return;
        }
        Properties p = new Properties();
        try (var reader = Files.newBufferedReader(Path.of(args[0]))) {
            p.load(reader);
        }
        verify(p);
        System.out.println("Chunk rebuild benchmark PASS: " + p);
    }

    private static void verify(Properties p) {
        if (!p.getProperty("passed", "false").equals("true")
                || !p.getProperty("state_update_recreated", "false").equals("true")
                || !p.getProperty("instanced.chair_quads", "missing").equals("0")
                || !p.getProperty("instanced.bust_quads", "missing").equals("0")
                || !p.getProperty("instanced.total_quads", "missing").equals("0")
                || !p.getProperty("native.backend", "missing").equals("flywheel:off")
                || p.getProperty("instanced.backend", "flywheel:off").equals("flywheel:off")
                || Double.parseDouble(p.getProperty("native.median_ms", "0")) <= 0
                || Double.parseDouble(p.getProperty("instanced.median_ms", "0")) <= 0) {
            throw new IllegalStateException("Missing or failed benchmark evidence: " + p);
        }
    }
}
