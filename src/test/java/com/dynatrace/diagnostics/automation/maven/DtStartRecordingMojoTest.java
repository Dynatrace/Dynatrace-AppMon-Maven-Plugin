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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.*;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.dynatrace.diagnostics.automation.maven.matchers.StartRecordingRequestProfileNameMatcher;
import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.sessions.Sessions;
import com.dynatrace.sdk.server.sessions.models.RecordingOption;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Sessions.class, DtStartRecording.class})
public class DtStartRecordingMojoTest extends AbstractDynatraceMojoTest<DtStartRecording> {
    private static final String START_RECORDING_GOAL_NAME = "startRecording";

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.applyFreshMojo();

        Sessions sessions = spy(new Sessions(this.getMojo().getDynatraceClient()));

        /** define responses */
        doReturn("example-session-location").when(sessions).startRecording(Mockito.argThat(new StartRecordingRequestProfileNameMatcher("start-recording-success")));
        doThrow(new ServerConnectionException("message", new Exception())).when(sessions).startRecording(Mockito.argThat(new StartRecordingRequestProfileNameMatcher("start-recording-with-exception")));

        whenNew(Sessions.class).withAnyArguments().thenReturn(sessions);

        /** verify default values */
        assertThat(this.getMojo().getRecordingOption(), is("all"));
    }

    @Override
    protected String getMojoGoalName() {
        return START_RECORDING_GOAL_NAME;
    }

    @Test
    public void testStartRecordingWithSuccess() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setMavenProject(new MavenProject());
            this.getMojo().setProfileName("start-recording-success");
            this.getMojo().execute();
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testStartRecordingWithException() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setMavenProject(new MavenProject());
            this.getMojo().setProfileName("start-recording-with-exception");
            this.getMojo().execute();

            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));
        }
    }

    @Test
    public void testStartRecordingWithSessionLocationPropertySet() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setMavenProject(new MavenProject());
            this.getMojo().setProfileName("start-recording-success");
            this.getMojo().setSessionLocationProperty("someProperty");

            this.getMojo().execute();

            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("someProperty"), is("example-session-location"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testStartRecordingProperties() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setSessionName("a");
            this.getMojo().setSessionDescription("b");
            this.getMojo().setRecordingOption(RecordingOption.ALL.getInternal());
            this.getMojo().setSessionLocationProperty("c");
            this.getMojo().setSessionLocked(true);
            this.getMojo().setAppendTimestamp(true);

            assertThat(this.getMojo().getSessionName(), is("a"));
            assertThat(this.getMojo().getSessionDescription(), is("b"));
            assertThat(this.getMojo().getRecordingOption(), is(RecordingOption.ALL.getInternal()));
            assertThat(this.getMojo().getSessionLocationProperty(), is("c"));
            assertThat(this.getMojo().isSessionLocked(), is(true));
            assertThat(this.getMojo().isAppendTimestamp(), is(true));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }
}