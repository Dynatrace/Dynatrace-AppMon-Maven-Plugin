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

import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.exceptions.ServerResponseException;
import com.dynatrace.sdk.server.memorydumps.MemoryDumps;
import com.dynatrace.sdk.server.memorydumps.models.JobState;
import com.dynatrace.sdk.server.memorydumps.models.MemoryDumpJob;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MemoryDumps.class, DtMemoryDump.class})
public class DtMemoryDumpMojoTest extends AbstractDynatraceMojoTest<DtMemoryDump> {
    private static final String MEMORY_DUMP_MOJO_NAME = "memoryDump";

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.applyFreshMojo();

        MemoryDumps memoryDumps = spy(new MemoryDumps(this.getMojo().getDynatraceClient()));

        /** define responses */
        doReturn("https://localhost:8021/rest/management/profiles/system-profile-success/memorydumpjobs/Memory%20Dump%20%5B11880540745601%5D")
                .when(memoryDumps).createMemoryDumpJob(Mockito.anyString(), Mockito.any(MemoryDumpJob.class));

        MemoryDumpJob memoryDumpJob = new MemoryDumpJob();
        memoryDumpJob.setState(JobState.FINISHED);

        MemoryDumpJob memoryDumpJob2 = new MemoryDumpJob();
        memoryDumpJob2.setState(JobState.RUNNING);

        doReturn(memoryDumpJob).when(memoryDumps).getMemoryDumpJob("system-profile-success", "Memory Dump [11880540745601]");
        doReturn(memoryDumpJob2).when(memoryDumps).getMemoryDumpJob("system-profile-timeout", "Memory Dump [11880540745601]");
        doThrow(new ServerConnectionException("message", new Exception())).when(memoryDumps).getMemoryDumpJob("system-profile-exception", "Memory Dump [11880540745601]");
        doThrow(new ServerResponseException(500, "message", new Exception())).when(memoryDumps).getMemoryDumpJob("system-profile-exception-continue", "Memory Dump [11880540745601]");

        whenNew(MemoryDumps.class).withAnyArguments().thenReturn(memoryDumps);

        /** verify default values */
        assertThat(this.getMojo().getDumpType(), is("memdump_simple"));
        assertThat(this.getMojo().isSessionLocked(), is(true));
        assertThat(this.getMojo().getDoGc(), is(false));
        assertThat(this.getMojo().getAutoPostProcess(), is(false));
        assertThat(this.getMojo().getCapturePrimitives(), is(false));
        assertThat(this.getMojo().getCaptureStrings(), is(false));
        assertThat(this.getMojo().getWaitForDumpTimeout(), is(60000));
        assertThat(this.getMojo().getWaitForDumpPollingInterval(), is(5000));
    }

    @Override
    protected String getMojoGoalName() {
        return MEMORY_DUMP_MOJO_NAME;
    }

    @Test
    public void testMemoryDumpWithSuccess() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setProfileName("system-profile-success");
            this.getMojo().setAgentName("agent-name");
            this.getMojo().setHostName("host-name");
            this.getMojo().setProcessId(1234);

            this.getMojo().setMemoryDumpNameProperty("dump-name");
            this.getMojo().setDumpStatusProperty("dump-status");

            this.getMojo().execute();

            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("dump-name"), is("Memory Dump [11880540745601]"));
            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("dump-status"), is("true"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testMemoryDumpWithTimeout() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setProfileName("system-profile-timeout");
            this.getMojo().setAgentName("agent-name");
            this.getMojo().setHostName("host-name");
            this.getMojo().setProcessId(1234);

            this.getMojo().setMemoryDumpNameProperty("dump-name");
            this.getMojo().setDumpStatusProperty("dump-status");

            this.getMojo().setWaitForDumpTimeout(100);
            this.getMojo().setWaitForDumpPollingInterval(10);

            this.getMojo().execute();

            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("dump-name"), is("Memory Dump [11880540745601]"));
            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("dump-status"), is("false"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testMemoryDumpWithException() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setProfileName("system-profile-exception");
            this.getMojo().setAgentName("agent-name");
            this.getMojo().setHostName("host-name");
            this.getMojo().setProcessId(1234);

            this.getMojo().setMemoryDumpNameProperty("dump-name");
            this.getMojo().setDumpStatusProperty("dump-status");

            this.getMojo().setWaitForDumpTimeout(100);
            this.getMojo().setWaitForDumpPollingInterval(10);

            this.getMojo().execute();

            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));
        }
    }

    @Test
    public void testMemoryDumpWithExceptionContinue() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setProfileName("system-profile-exception-continue");
            this.getMojo().setAgentName("agent-name");
            this.getMojo().setHostName("host-name");
            this.getMojo().setProcessId(1234);

            this.getMojo().setMemoryDumpNameProperty("dump-name");
            this.getMojo().setDumpStatusProperty("dump-status");

            this.getMojo().setWaitForDumpTimeout(100);
            this.getMojo().setWaitForDumpPollingInterval(10);

            this.getMojo().execute();

            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("dump-name"), is("Memory Dump [11880540745601]"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }


    @Test
    public void testMemoryDumpWithoutProperties() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().execute();

            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(NullPointerException.class));
        }
    }
}