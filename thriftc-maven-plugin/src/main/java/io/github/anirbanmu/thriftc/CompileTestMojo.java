package io.github.anirbanmu.thriftc;

import java.io.File;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "compile-test", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES)
public class CompileTestMojo extends AbstractThriftMojo {

    @Parameter(defaultValue = "${project.basedir}/src/test/thrift", property = "thriftc.testThriftSourceDir")
    private File thriftSourceDir;

    @Parameter(defaultValue = "${project.build.directory}/generated-test-sources/thrift", property = "thriftc.testOutputDirectory")
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
        project.addTestCompileSourceRoot(path);
    }
}
