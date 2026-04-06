package io.github.anirbanmu.thriftc;

import javax.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

abstract class AbstractThriftMojo extends AbstractMojo {

    @Parameter(defaultValue = "0.22.0", property = "thriftc.thriftVersion")
    protected String thriftVersion;

    @Parameter(defaultValue = "java", property = "thriftc.generator")
    protected String generator;

    @Parameter
    protected List<File> includePaths;

    @Parameter(defaultValue = "false", property = "thriftc.recursive")
    protected boolean recursive;

    @Parameter(defaultValue = "300", property = "thriftc.timeoutSeconds")
    protected int timeoutSeconds;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Inject
    protected RepositorySystem repoSystem;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
    protected RepositorySystemSession repoSession;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true, required = true)
    protected List<RemoteRepository> remoteRepositories;

    protected abstract File getThriftSourceDir();

    protected abstract File getOutputDirectory();

    protected abstract void addSourceRoot(String path);

    @Override
    public void execute() throws MojoExecutionException {
        PlatformDetector.Platform platform;
        try {
            platform = PlatformDetector.detect();
        } catch (IllegalStateException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        File binary = BinaryResolver.resolve(platform, thriftVersion, repoSystem, repoSession, remoteRepositories);

        File sourceDir = getThriftSourceDir();
        if (!sourceDir.isDirectory()) {
            getLog().info("thrift source directory does not exist: " + sourceDir + ", skipping");
            return;
        }

        List<File> thriftFiles;
        try (Stream<Path> stream = Files.walk(sourceDir.toPath())) {
            thriftFiles = stream
                    .filter(p -> p.toString().endsWith(".thrift"))
                    .map(Path::toFile)
                    .toList();
        } catch (IOException e) {
            throw new MojoExecutionException("failed to scan thrift source directory: " + sourceDir, e);
        }

        if (thriftFiles.isEmpty()) {
            getLog().info("no .thrift files found in " + sourceDir + ", skipping");
            return;
        }

        File outputDir = getOutputDirectory();
        outputDir.mkdirs();

        CompilerInvoker invoker = new CompilerInvoker(
                binary, generator, outputDir,
                // maven sets unset list parameters to null rather than empty
                includePaths != null ? includePaths : List.of(),
                recursive, timeoutSeconds, getLog());

        for (File thriftFile : thriftFiles) {
            invoker.compile(thriftFile);
        }

        if (generator.equals("java") || generator.startsWith("java:")) {
            addSourceRoot(outputDir.getAbsolutePath());
        }
    }
}
