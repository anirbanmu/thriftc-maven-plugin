package io.github.anirbanmu.thriftc;

import java.io.File;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "compile", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class CompileMojo extends AbstractThriftMojo {

    @Parameter(defaultValue = "${project.basedir}/src/main/thrift", property = "thriftc.thriftSourceDir")
    private File thriftSourceDir;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/thrift", property = "thriftc.outputDirectory")
    private File outputDirectory;

    @Override
    protected File getThriftSourceDir() {
        return thriftSourceDir;
    }

    @Override
    protected File getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    protected void addSourceRoot(String path) {
        project.addCompileSourceRoot(path);
    }
}
