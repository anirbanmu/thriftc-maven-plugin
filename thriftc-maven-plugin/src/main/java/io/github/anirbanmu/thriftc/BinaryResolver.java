package io.github.anirbanmu.thriftc;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

public final class BinaryResolver {

    private static final String GROUP_ID = "io.github.anirbanmu";
    private static final String ARTIFACT_ID = "thriftc-jar";
    private static final String TYPE = "exe";

    public static File resolve(PlatformDetector.Platform platform, String thriftVersion,
                               RepositorySystem repoSystem, RepositorySystemSession session,
                               List<RemoteRepository> remoteRepos) throws MojoExecutionException {
        String coords = GROUP_ID + ":" + ARTIFACT_ID + ":" + TYPE + ":" + platform.classifier() + ":" + thriftVersion;

        Artifact artifact = new DefaultArtifact(GROUP_ID, ARTIFACT_ID, platform.classifier(), TYPE, thriftVersion);
        ArtifactRequest request = new ArtifactRequest(artifact, remoteRepos, null);

        ArtifactResult result;
        try {
            result = repoSystem.resolveArtifact(session, request);
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException("failed to resolve thriftc binary: " + coords, e);
        }

        File file = result.getArtifact().getFile();

        if (platform.os() != PlatformDetector.Os.WINDOWS) {
            if (!file.setExecutable(true)) {
                throw new MojoExecutionException(
                    "failed to set executable permission on: " + file.getAbsolutePath());
            }
        }

        return file;
    }

    private BinaryResolver() {}
}
