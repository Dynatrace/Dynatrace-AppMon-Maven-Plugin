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
    @Parameter(property = "dynaTrace.sessionNameProperty")
    private String sessionNameProperty;

    @Parameter(property = "dynaTrace.doReanalyzeSession", defaultValue = "false")
    private boolean doReanalyzeSession;

    @Parameter(property = "dynaTrace.reanalyzeSessionTimeout", defaultValue = "60000")
    private int reanalyzeSessionTimeout;

    @Parameter(property = "dynaTrace.reanalyzeSessionPollingInterval", defaultValue = "5000")
    private int reanalyzeSessionPollingInterval;

    @Parameter(property = "dynaTrace.stopDelay", defaultValue = "0")
    private int stopDelay;

    @Parameter(property = "dynaTrace.reanalyzeStatusProperty")
    private String reanalyzeStatusProperty;

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

        try {
            Sessions sessions = new Sessions(this.getDynatraceClient());
            String sessionName = sessions.stopRecording(this.getProfileName());

            this.getLog().info(String.format("Stopped recording on %s with SessionName %s", this.getProfileName(), sessionName));

            if (!DtUtil.isEmpty(this.sessionNameProperty)) {
                this.getMavenProject().getProperties().setProperty(getSessionNameProperty(), sessionName);
            }

            if (this.doReanalyzeSession) {
                boolean reanalyzeFinished = sessions.getReanalysisStatus(sessionName);

                if (sessions.reanalyze(sessionName)) {
                    int timeout = this.reanalyzeSessionTimeout;

                    while (!reanalyzeFinished && (timeout > 0)) {
                        try {
                            Thread.sleep(this.reanalyzeSessionPollingInterval);
                            timeout -= this.reanalyzeSessionPollingInterval;
                        } catch (InterruptedException e) {
                            /* don't break execution */
                        }

                        reanalyzeFinished = sessions.getReanalysisStatus(sessionName);
                    }
                }

                if (!DtUtil.isEmpty(this.reanalyzeStatusProperty)) {
                    this.getMavenProject().getProperties().setProperty(this.reanalyzeStatusProperty, String.valueOf(reanalyzeFinished));
                }
            }
        } catch (ServerConnectionException | ServerResponseException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    public String getSessionNameProperty() {
        return sessionNameProperty;
    }

    public void setSessionNameProperty(String sessionNameProperty) {
        this.sessionNameProperty = sessionNameProperty;
    }

    public boolean isDoReanalyzeSession() {
        return doReanalyzeSession;
    }

    public void setDoReanalyzeSession(boolean doReanalyzeSession) {
        this.doReanalyzeSession = doReanalyzeSession;
    }

    public int getReanalyzeSessionTimeout() {
        return reanalyzeSessionTimeout;
    }

    public void setReanalyzeSessionTimeout(int reanalyzeSessionTimeout) {
        this.reanalyzeSessionTimeout = reanalyzeSessionTimeout;
    }

    public String getReanalyzeStatusProperty() {
        return reanalyzeStatusProperty;
    }

    public void setReanalyzeStatusProperty(String reanalyzeStatusProperty) {
        this.reanalyzeStatusProperty = reanalyzeStatusProperty;
    }

    public int getReanalyzeSessionPollingInterval() {
        return reanalyzeSessionPollingInterval;
    }

    public void setReanalyzeSessionPollingInterval(
            int reanalyzeSessionPollingInterval) {
        this.reanalyzeSessionPollingInterval = reanalyzeSessionPollingInterval;
    }

    public int getStopDelay() {
        return stopDelay;
    }

    public void setStopDelay(int stopDelay) {
        this.stopDelay = stopDelay;
    }
}
