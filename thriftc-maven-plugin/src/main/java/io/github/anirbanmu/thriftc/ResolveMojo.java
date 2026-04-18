package io.github.anirbanmu.thriftc;

import javax.inject.Inject;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

// pre-fetches the platform-specific thriftc binary into the local maven repository
// so that subsequent offline builds (mvn -o) can resolve it without network access
@Mojo(name = "resolve")
public class ResolveMojo extends AbstractMojo {

    @Parameter(defaultValue = "0.22.0", property = "thriftc.thriftVersion")
    private String thriftVersion;

    @Inject
    private RepositorySystem repoSystem;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
    private RepositorySystemSession repoSession;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true, required = true)
    private List<RemoteRepository> remoteRepositories;

    @Override
    public void execute() throws MojoExecutionException {
        PlatformDetector.Platform platform;
        try {
            platform = PlatformDetector.detect();
        } catch (IllegalStateException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        getLog().info("detected platform: " + platform.classifier());

        File binary = BinaryResolver.resolve(platform, thriftVersion, repoSystem, repoSession, remoteRepositories);

        getLog().info("pre-fetched thriftc " + thriftVersion + " binary to local repository: " + binary.getAbsolutePath());
    }
}
