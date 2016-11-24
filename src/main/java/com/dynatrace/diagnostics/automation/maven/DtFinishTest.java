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

import com.dynatrace.diagnostics.automation.util.DtUtil;
import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.exceptions.ServerResponseException;
import com.dynatrace.sdk.server.sessions.Sessions;
import com.dynatrace.sdk.server.sessions.models.RecordingOption;
import com.dynatrace.sdk.server.sessions.models.StartRecordingRequest;
import com.dynatrace.sdk.server.testautomation.TestAutomation;
import org.apache.maven.exception.ExceptionSummary;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.Properties;

/**
 * Implements "startRecording" Maven goal
 */
@Mojo(name = "finishTest", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class DtFinishTest extends DtServerProfileBase {

    public static final String TESTRUN_ID_PROPERTY_NAME = "finishTest";

    /* Properties with default values available in Maven Project environment */
    @Parameter(property = "dynaTrace.testRunId")
    private String testRunId = null;

    /**
     * Executes maven goal
     *
     * @throws MojoExecutionException whenever connecting to the server, parsing a response or execution fails
     */
    public void execute() throws MojoExecutionException {
        try {
            String systemProfile = this.getProfileName();
            String testRunId = this.getTestRunId();

            if (testRunId == null) {
                Properties properties = this.getMavenProject().getProperties();
                testRunId = properties.getProperty(TESTRUN_ID_PROPERTY_NAME);
            }
            if (testRunId == null || systemProfile == null) {
                throw new IllegalArgumentException(String.format("Error due to empty value, testRun profile %s, testRun ID='%s' ", systemProfile, testRunId));
            }

            TestAutomation testAutomation = new TestAutomation(this.getDynatraceClient());
            testAutomation.finishTestRun(systemProfile, testRunId);
            this.getLog().info(String.format("Finish testRun profile %s testRun ID=%s", systemProfile, testRunId));
        } catch (ServerConnectionException | ServerResponseException e) {
            throw new MojoExecutionException(String.format("Error while trying to finish testRun profile %s, testRun ID='%s' ", this.getProfileName(), testRunId, e));
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    public String getTestRunId() {
        return testRunId;
    }

    public void setTestRunId(String testRunId) {
        this.testRunId = testRunId;
    }

}
