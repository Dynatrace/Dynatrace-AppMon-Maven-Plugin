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
import com.dynatrace.sdk.server.resourcedumps.ResourceDumps;
import com.dynatrace.sdk.server.resourcedumps.models.CreateThreadDumpRequest;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Implements "threadDump" Maven goal
 */
@Mojo(name = "threadDump", defaultPhase = LifecyclePhase.VERIFY)
public class DtThreadDump extends DtAgentBase {

    /* Properties with default values available in Maven Project environment */
    @Parameter(property = "dynaTrace.sessionLocked")
    private boolean sessionLocked;

    @Parameter(property = "dynaTrace.threadDumpNameProperty")
    private String threadDumpNameProperty;

    @Parameter(property = "dynaTrace.waitForDumpTimeout", defaultValue = "60000")
    private int waitForDumpTimeout;

    @Parameter(property = "dynaTrace.waitForDumpPollingInterval", defaultValue = "5000")
    private int waitForDumpPollingInterval;

    @Parameter(property = "dynaTrace.dumpStatusProperty")
    private String dumpStatusProperty;

    /**
     * Executes maven goal
     *
     * @throws MojoExecutionException whenever connecting to the server, parsing a response or execution fails
     */
    public void execute() throws MojoExecutionException {
        this.getLog().info(String.format("Creating Thread Dump for %s-%s-%s-%d", this.getProfileName(), this.getAgentName(), this.getHostName(), this.getProcessId()));

        ResourceDumps resourceDumps = new ResourceDumps(this.getDynatraceClient());
        CreateThreadDumpRequest createThreadDumpRequest = new CreateThreadDumpRequest(this.getProfileName(), this.getAgentName(), this.getHostName(), this.getProcessId());
        createThreadDumpRequest.setSessionLocked(this.isSessionLocked());

        try {
            String threadDump = resourceDumps.createThreadDump(createThreadDumpRequest);

            if (!DtUtil.isEmpty(this.threadDumpNameProperty)) {
                this.getMavenProject().getProperties().setProperty(this.threadDumpNameProperty, threadDump);
            }

            int timeout = this.waitForDumpTimeout;
            boolean dumpFinished = Boolean.TRUE.equals(resourceDumps.getThreadDumpStatus(this.getProfileName(), threadDump).getResultValue());

            while (!dumpFinished && (timeout > 0)) {
                try {
                    java.lang.Thread.sleep(this.waitForDumpPollingInterval);
                    timeout -= this.waitForDumpPollingInterval;
                } catch (InterruptedException e) {
                    /* don't break execution */
                }

                dumpFinished = Boolean.TRUE.equals(resourceDumps.getThreadDumpStatus(this.getProfileName(), threadDump).getResultValue());
            }

            if (!DtUtil.isEmpty(this.dumpStatusProperty)) {
                this.getMavenProject().getProperties().setProperty(this.dumpStatusProperty, String.valueOf(dumpFinished));
            }
        } catch (ServerConnectionException | ServerResponseException e) {
            throw new MojoExecutionException(String.format("Error while trying to create thread dump: %s", e.getMessage()), e);
        }
    }

    public boolean isSessionLocked() {
        return sessionLocked;
    }

    public void setSessionLocked(boolean sessionLocked) {
        this.sessionLocked = sessionLocked;
    }

    public String getThreadDumpNameProperty() {
        return threadDumpNameProperty;
    }

    public void setThreadDumpNameProperty(String threadDumpNameProperty) {
        this.threadDumpNameProperty = threadDumpNameProperty;
    }

    public int getWaitForDumpTimeout() {
        return waitForDumpTimeout;
    }

    public void setWaitForDumpTimeout(int waitForDumpTimeout) {
        this.waitForDumpTimeout = waitForDumpTimeout;
    }

    public int getWaitForDumpPollingInterval() {
        return waitForDumpPollingInterval;
    }

    public void setWaitForDumpPollingInterval(int waitForDumpPollingInterval) {
        this.waitForDumpPollingInterval = waitForDumpPollingInterval;
    }

    public String getDumpStatusProperty() {
        return dumpStatusProperty;
    }

    public void setDumpStatusProperty(String dumpStatusProperty) {
        this.dumpStatusProperty = dumpStatusProperty;
    }
}
