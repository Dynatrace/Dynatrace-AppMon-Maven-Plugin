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

        Sessions sessions = spy(new Sessions(mojo.getDynatraceClient()));

        /** define responses */
        doReturn("example-session-name").when(sessions).startRecording(Mockito.argThat(new StartRecordingRequestProfileNameMatcher("start-recording-success")));
        doThrow(new ServerConnectionException("message", new Exception())).when(sessions).startRecording(Mockito.argThat(new StartRecordingRequestProfileNameMatcher("start-recording-with-exception")));

        whenNew(Sessions.class).withAnyArguments().thenReturn(sessions);

        /** verify default values */
        assertThat(mojo.getRecordingOption(), is("all"));
    }

    @Override
    protected String getMojoGoalName() {
        return START_RECORDING_GOAL_NAME;
    }

    @Test
    public void testStartRecordingWithSuccess() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setMavenProject(new MavenProject());
            mojo.setProfileName("start-recording-success");
            mojo.execute();
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testStartRecordingWithException() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setMavenProject(new MavenProject());
            mojo.setProfileName("start-recording-with-exception");
            mojo.execute();

            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));
        }
    }

    @Test
    public void testStartRecordingWithSessionNamePropertySet() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setMavenProject(new MavenProject());
            mojo.setProfileName("start-recording-success");
            mojo.setSessionNameProperty("someProperty");

            mojo.execute();

            assertThat(mojo.getMavenProject().getProperties().getProperty("someProperty"), is("example-session-name"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testStartRecordingProperties() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setSessionName("a");
            mojo.setSessionDescription("b");
            mojo.setRecordingOption(RecordingOption.ALL.getInternal());
            mojo.setSessionNameProperty("c");
            mojo.setSessionLocked(true);
            mojo.setAppendTimestamp(true);

            assertThat(mojo.getSessionName(), is("a"));
            assertThat(mojo.getSessionDescription(), is("b"));
            assertThat(mojo.getRecordingOption(), is(RecordingOption.ALL.getInternal()));
            assertThat(mojo.getSessionNameProperty(), is("c"));
            assertThat(mojo.isSessionLocked(), is(true));
            assertThat(mojo.isAppendTimestamp(), is(true));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }
}