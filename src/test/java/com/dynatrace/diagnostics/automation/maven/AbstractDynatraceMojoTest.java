package com.dynatrace.diagnostics.automation.maven;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.eclipse.aether.DefaultRepositorySystemSession;

import java.io.File;

import static org.powermock.api.mockito.PowerMockito.spy;

public abstract class AbstractDynatraceMojoTest<T extends DtServerBase> extends AbstractMojoTestCase {
    public static final String DEFAULT_TEST_PLUGIN_CONFIG_PATH = "src/test/resources/unit/basic-test/basic-test-plugin-config.xml";

    /** tested mojo */
    protected T mojo;

    protected Mojo lookupCustomConfiguredMojo(String mojoName) throws Exception {
        File pomFile = new File(getBasedir(), DEFAULT_TEST_PLUGIN_CONFIG_PATH);
        MavenExecutionRequest executionRequest = new DefaultMavenExecutionRequest();

        ProjectBuildingRequest buildingRequest = executionRequest.getProjectBuildingRequest().setRepositorySession(new DefaultRepositorySystemSession());
        ProjectBuilder projectBuilder = lookup(ProjectBuilder.class);

        MavenProject project = projectBuilder.build(pomFile, buildingRequest).getProject();

        return this.lookupConfiguredMojo(project, mojoName);
    }

    protected void applyFreshMojo() throws Exception {
        mojo = spy((T) this.lookupCustomConfiguredMojo(this.getMojoGoalName()));
        mojo.setDynatraceClientWithCustomHttpClient(null);
    }

    protected abstract String getMojoGoalName();
}
