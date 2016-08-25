package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.diagnostics.automation.maven.matchers.StartRecordingRequestProfileNameMatcher;
import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.sessions.Sessions;
import com.dynatrace.sdk.server.sessions.models.RecordingOption;
import com.dynatrace.sdk.server.sessions.models.StartRecordingRequest;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
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
@PrepareForTest({Sessions.class, DtStartRecording.class})
public class DtStartRecordingMojoTest extends AbstractDynatraceMojoTest<DtStartRecording> {
    private static final String START_RECORDING_GOAL_NAME = "startRecording";

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.applyFreshMojo();
        StartRecordingRequest startRecordingRequest = new StartRecordingRequest("system-profile-name");

        Sessions sessions = spy(new Sessions(this.getMojo().getDynatraceClient()));

        /** define responses */
        doReturn("example-session-name").when(sessions).startRecording(Mockito.argThat(new StartRecordingRequestProfileNameMatcher("start-recording-success")));
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
    public void testStartRecordingWithSessionNamePropertySet() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setMavenProject(new MavenProject());
            this.getMojo().setProfileName("start-recording-success");
            this.getMojo().setSessionNameProperty("someProperty");

            this.getMojo().execute();

            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("someProperty"), is("example-session-name"));
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
            this.getMojo().setSessionNameProperty("c");
            this.getMojo().setSessionLocked(true);
            this.getMojo().setAppendTimestamp(true);

            assertThat(this.getMojo().getSessionName(), is("a"));
            assertThat(this.getMojo().getSessionDescription(), is("b"));
            assertThat(this.getMojo().getRecordingOption(), is(RecordingOption.ALL.getInternal()));
            assertThat(this.getMojo().getSessionNameProperty(), is("c"));
            assertThat(this.getMojo().isSessionLocked(), is(true));
            assertThat(this.getMojo().isAppendTimestamp(), is(true));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }
}