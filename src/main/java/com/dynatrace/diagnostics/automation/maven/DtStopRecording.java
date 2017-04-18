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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Implements "stopRecording" Maven goal
 */
@Mojo(name = "stopRecording", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class DtStopRecording extends DtServerProfileBase {

    /* Properties with default values available in Maven Project environment */
    @Parameter(property = "dynaTrace.sessionUriProperty")
    private String sessionUriProperty;

    @Parameter(property = "dynaTrace.stopDelay", defaultValue = "0")
    private int stopDelay;


    /**
     * Executes maven goal
     *
     * @throws MojoExecutionException whenever connecting to the server, parsing a response or execution fails
     */
    public void execute() throws MojoExecutionException {
        try {
            Thread.sleep(this.stopDelay);
        } catch (InterruptedException e) {
            /* don't break execution */
        }

        String sessionUri = null;

        try {
            Sessions sessions = new Sessions(this.getDynatraceClient());
            sessionUri = sessions.stopRecording(this.getProfileName());

            this.getLog().info(String.format("Stopped recording on %s with session URI %s", this.getProfileName(), sessionUri));

            if (!DtUtil.isEmpty(this.sessionUriProperty)) {
                this.getMavenProject().getProperties().setProperty(this.sessionUriProperty, sessionUri);
            }

        } catch (ServerConnectionException | ServerResponseException e) {
            throw new MojoExecutionException(String.format("Error while trying to stop recording of session %s on profile %s.", sessionUri, getProfileName(), e.getMessage()), e);
        }
    }

    public String getSessionUriProperty() {
        return sessionUriProperty;
    }

    public void setSessionUriProperty(String sessionUriProperty) {
        this.sessionUriProperty = sessionUriProperty;
    }

    public int getStopDelay() {
        return stopDelay;
    }

    public void setStopDelay(int stopDelay) {
        this.stopDelay = stopDelay;
    }
}
