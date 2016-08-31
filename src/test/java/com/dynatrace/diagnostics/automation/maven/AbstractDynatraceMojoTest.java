/*
 * Dynatrace Maven Plugin
 * Copyright (c) 2008-2016, DYNATRACE LLC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *  Neither the name of the dynaTrace software nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

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

    private T mojo;

    protected Mojo lookupCustomConfiguredMojo(String mojoName) throws Exception {
        File pomFile = new File(getBasedir(), DEFAULT_TEST_PLUGIN_CONFIG_PATH);
        MavenExecutionRequest executionRequest = new DefaultMavenExecutionRequest();

        ProjectBuildingRequest buildingRequest = executionRequest.getProjectBuildingRequest().setRepositorySession(new DefaultRepositorySystemSession());
        ProjectBuilder projectBuilder = lookup(ProjectBuilder.class);

        MavenProject project = projectBuilder.build(pomFile, buildingRequest).getProject();

        return this.lookupConfiguredMojo(project, mojoName);
    }

    @SuppressWarnings("unchecked")
    protected void applyFreshMojo() throws Exception {
        this.mojo = spy((T) this.lookupCustomConfiguredMojo(this.getMojoGoalName()));
        this.getMojo().setDynatraceClientWithCustomHttpClient(null);
    }

    protected abstract String getMojoGoalName();

    protected T getMojo() {
        return this.mojo;
    }
}
