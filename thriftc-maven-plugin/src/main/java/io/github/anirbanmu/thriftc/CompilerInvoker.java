package io.github.anirbanmu.thriftc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

public class CompilerInvoker {

    private final File binary;
    private final String generator;
    private final File outputDirectory;
    private final List<File> includePaths;
    private final boolean recursive;
    private final int timeoutSeconds;
    private final Log log;

    public CompilerInvoker(File binary, String generator, File outputDirectory,
                           List<File> includePaths, boolean recursive,
                           int timeoutSeconds, Log log) {
        this.binary = binary;
        this.generator = generator;
        this.outputDirectory = outputDirectory;
        this.includePaths = List.copyOf(includePaths);
        this.recursive = recursive;
        this.timeoutSeconds = timeoutSeconds;
        this.log = log;
    }

    public void compile(File thriftFile) throws MojoExecutionException {
        List<String> command = buildCommandLine(thriftFile);
        log.debug("executing: " + String.join(" ", command));

        try {
            Process process = new ProcessBuilder(command).start();

            // drain stderr on a separate thread so a full pipe buffer can't deadlock waitFor
            CompletableFuture<String> stderrFuture = CompletableFuture.supplyAsync(
                    () -> readStream(process.getErrorStream()));

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new MojoExecutionException(
                        "thrift compiler timed out after " + timeoutSeconds + " seconds");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                String stderr;
                try {
                    stderr = stderrFuture.join();
                } catch (CompletionException e) {
                    stderr = "<stderr unavailable: " + e.getCause().getMessage() + ">";
                }
                throw new MojoExecutionException(
                        "thrift compiler failed with exit code " + exitCode + ": " + stderr);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("failed to execute thrift compiler: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MojoExecutionException("thrift compiler execution interrupted", e);
        }
    }

    // package-private for testing
    List<String> buildCommandLine(File thriftFile) {
        List<String> command = new ArrayList<>();
        command.add(binary.getAbsolutePath());
        command.add("--gen");
        command.add(generator);
        command.add("-o");
        command.add(outputDirectory.getAbsolutePath());

        for (File includePath : includePaths) {
            command.add("-I");
            command.add(includePath.getAbsolutePath());
        }

        if (recursive) {
            command.add("-r");
        }

        command.add(thriftFile.getAbsolutePath());
        return List.copyOf(command);
    }

    private static String readStream(InputStream stream) {
        try {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
