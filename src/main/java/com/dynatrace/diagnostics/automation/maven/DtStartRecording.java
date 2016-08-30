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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Implements "startRecording" Maven goal
 */
@Mojo(name = "startRecording", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class DtStartRecording extends DtServerProfileBase {

    /* Properties with default values available in Maven Project environment */
    @Parameter(property = "dynaTrace.sessionName")
    private String sessionName;

    @Parameter(property = "dynaTrace.sessionDescription")
    private String sessionDescription;

    @Parameter(property = "dynaTrace.recordingOption", required = true, defaultValue = "all")
    private String recordingOption;

    @Parameter(property = "dynaTrace.sessionNameProperty")
    private String sessionNameProperty;

    @Parameter(property = "dynaTrace.sessionLocked")
    private boolean sessionLocked;

    @Parameter(property = "dynaTrace.appendTimestamp")
    private boolean appendTimestamp;

    /**
     * Executes maven goal
     *
     * @throws MojoExecutionException whenever connecting to the server, parsing a response or execution fails
     */
    public void execute() throws MojoExecutionException {
        Sessions sessions = new Sessions(this.getDynatraceClient());

        StartRecordingRequest startRecordingRequest = new StartRecordingRequest(this.getProfileName());
        startRecordingRequest.setPresentableName(this.sessionName);
        startRecordingRequest.setDescription(this.sessionDescription);
        startRecordingRequest.setSessionLocked(this.sessionLocked);
        startRecordingRequest.setTimestampAllowed(this.appendTimestamp);

        if (this.recordingOption != null) {
            startRecordingRequest.setRecordingOption(RecordingOption.fromInternal(this.recordingOption));
        }

        try {
            String sessionName = sessions.startRecording(startRecordingRequest);
            this.getLog().info(String.format("Started recording on %s with SessionName %s", this.getProfileName(), sessionName));

            if (!DtUtil.isEmpty(this.sessionNameProperty)) {
                this.getMavenProject().getProperties().setProperty(this.sessionNameProperty, sessionName);
            }
        } catch (ServerConnectionException | ServerResponseException e) {
            throw new MojoExecutionException(String.format("Error while trying to start recording in '%s' system profile: %s", this.getProfileName(), e.getMessage()), e);
        }
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getSessionDescription() {
        return sessionDescription;
    }

    public void setSessionDescription(String sessionDescription) {
        this.sessionDescription = sessionDescription;
    }

    public String getRecordingOption() {
        return recordingOption;
    }

    public void setRecordingOption(String recordingOption) {
        this.recordingOption = recordingOption;
    }

    public boolean isSessionLocked() {
        return sessionLocked;
    }

    public void setSessionLocked(boolean sessionLocked) {
        this.sessionLocked = sessionLocked;
    }

    public boolean isAppendTimestamp() {
        return appendTimestamp;
    }

    public void setAppendTimestamp(boolean appendTimestamp) {
        this.appendTimestamp = appendTimestamp;
    }

    public String getSessionNameProperty() {
        return sessionNameProperty;
    }

    public void setSessionNameProperty(String sessionNameProperty) {
        this.sessionNameProperty = sessionNameProperty;
    }
}
