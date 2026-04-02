package io.github.anirbanmu.thriftc;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompilerInvokerTest {

    private static final File BINARY = new File("/usr/local/bin/thrift");
    private static final File OUTPUT_DIR = new File("/tmp/generated");
    private static final File THRIFT_FILE = new File("/src/main/thrift/service.thrift");

    @Test
    void buildsDefaultCommandLine() {
        CompilerInvoker invoker = new CompilerInvoker(
                BINARY, "java", OUTPUT_DIR, List.of(), false, 300, null);

        List<String> command = invoker.buildCommandLine(THRIFT_FILE);

        assertEquals(List.of(
                BINARY.getAbsolutePath(),
                "--gen", "java",
                "-o", OUTPUT_DIR.getAbsolutePath(),
                THRIFT_FILE.getAbsolutePath()), command);
    }

    @Test
    void usesCustomGeneratorPy() {
        CompilerInvoker invoker = new CompilerInvoker(
                BINARY, "py", OUTPUT_DIR, List.of(), false, 300, null);

        List<String> command = invoker.buildCommandLine(THRIFT_FILE);

        assertEquals(List.of(
                BINARY.getAbsolutePath(),
                "--gen", "py",
                "-o", OUTPUT_DIR.getAbsolutePath(),
                THRIFT_FILE.getAbsolutePath()), command);
    }

    @Test
    void usesCustomGeneratorWithOptions() {
        CompilerInvoker invoker = new CompilerInvoker(
                BINARY, "java:beans,hashcode", OUTPUT_DIR, List.of(), false, 300, null);

        List<String> command = invoker.buildCommandLine(THRIFT_FILE);

        assertEquals(List.of(
                BINARY.getAbsolutePath(),
                "--gen", "java:beans,hashcode",
                "-o", OUTPUT_DIR.getAbsolutePath(),
                THRIFT_FILE.getAbsolutePath()), command);
    }

    @Test
    void includesSingleIncludePath() {
        File includePath = new File("/includes/shared");
        CompilerInvoker invoker = new CompilerInvoker(
                BINARY, "java", OUTPUT_DIR, List.of(includePath), false, 300, null);

        List<String> command = invoker.buildCommandLine(THRIFT_FILE);

        assertEquals(List.of(
                BINARY.getAbsolutePath(),
                "--gen", "java",
                "-o", OUTPUT_DIR.getAbsolutePath(),
                "-I", includePath.getAbsolutePath(),
                THRIFT_FILE.getAbsolutePath()), command);
    }

    @Test
    void includesMultipleIncludePathsInOrder() {
        File first = new File("/includes/common");
        File second = new File("/includes/shared");
        File third = new File("/includes/vendor");
        CompilerInvoker invoker = new CompilerInvoker(
                BINARY, "java", OUTPUT_DIR, List.of(first, second, third), false, 300, null);

        List<String> command = invoker.buildCommandLine(THRIFT_FILE);

        assertEquals(List.of(
                BINARY.getAbsolutePath(),
                "--gen", "java",
                "-o", OUTPUT_DIR.getAbsolutePath(),
                "-I", first.getAbsolutePath(),
                "-I", second.getAbsolutePath(),
                "-I", third.getAbsolutePath(),
                THRIFT_FILE.getAbsolutePath()), command);
    }

    @Test
    void emptyIncludePathsProducesNoIFlags() {
        CompilerInvoker invoker = new CompilerInvoker(
                BINARY, "java", OUTPUT_DIR, List.of(), false, 300, null);

        List<String> command = invoker.buildCommandLine(THRIFT_FILE);

        assertFalse(command.contains("-I"), "command should not contain -I when include paths are empty");
    }

    @Test
    void includesRecursiveFlagWhenEnabled() {
        CompilerInvoker invoker = new CompilerInvoker(
                BINARY, "java", OUTPUT_DIR, List.of(), true, 300, null);

        List<String> command = invoker.buildCommandLine(THRIFT_FILE);

        assertTrue(command.contains("-r"), "command should contain -r when recursive is true");
    }

    @Test
    void combinesAllFlagsWithCorrectOrdering() {
        File include1 = new File("/includes/common");
        File include2 = new File("/includes/shared");
        CompilerInvoker invoker = new CompilerInvoker(
                BINARY, "java:beans,hashcode", OUTPUT_DIR, List.of(include1, include2), true, 300, null);

        List<String> command = invoker.buildCommandLine(THRIFT_FILE);

        assertEquals(List.of(
                BINARY.getAbsolutePath(),
                "--gen", "java:beans,hashcode",
                "-o", OUTPUT_DIR.getAbsolutePath(),
                "-I", include1.getAbsolutePath(),
                "-I", include2.getAbsolutePath(),
                "-r",
                THRIFT_FILE.getAbsolutePath()), command);
    }
}
