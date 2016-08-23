package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.sessions.Sessions;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Sessions.class, DtStopRecording.class})
public class DtStopRecordingMojoTest extends AbstractDynatraceMojoTest<DtStopRecording> {
    private static final String STOP_RECORDING_GOAL_NAME = "stopRecording";

    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.applyFreshMojo();

        Sessions sessions = spy(new Sessions(mojo.getDynatraceClient()));

        /** define responses */
        doReturn("example-session-name").when(sessions).stopRecording("stop-recording");
        doReturn(true).when(sessions).reanalyze("example-session-name");
        doReturn(true).when(sessions).getReanalysisStatus("example-session-name");

        doThrow(new ServerConnectionException("message", new Exception())).when(sessions).stopRecording("stop-recording-with-exception");

        whenNew(Sessions.class).withAnyArguments().thenReturn(sessions);

        /** verify default values */
        assertThat(mojo.isDoReanalyzeSession(), is(false));
        assertThat(mojo.getReanalyzeSessionTimeout(), is(60000));
        assertThat(mojo.getReanalyzeSessionPollingInterval(), is(5000));
        assertThat(mojo.getStopDelay(), is(0));
    }

    @Override
    protected String getMojoGoalName() {
        return STOP_RECORDING_GOAL_NAME;
    }

    @Test
    public void testStopRecordingWithSuccess() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setMavenProject(new MavenProject());
            mojo.getMavenProject().getProperties().setProperty("dtSessionNameProperty", "other-session-name-property");
            mojo.setSessionNameProperty("session-name-property");
            mojo.setProfileName("stop-recording");
            mojo.execute();

            assertThat(mojo.getMavenProject().getProperties().getProperty("session-name-property"), is("example-session-name"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }


    @Test
    public void testStopRecordingWithSessionNamePropertyFromProject() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setMavenProject(new MavenProject());
            mojo.getMavenProject().getProperties().setProperty("dtSessionNameProperty", "session-name-property");
            mojo.setProfileName("stop-recording");
            mojo.execute();

            assertThat(mojo.getMavenProject().getProperties().getProperty("session-name-property"), is("example-session-name"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testStopRecordingWithReanalyzeSession() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setMavenProject(new MavenProject());
            mojo.setProfileName("stop-recording");
            mojo.setSessionNameProperty("session-name-property");
            mojo.setReanalyzeStatusProperty("reanalyze-status-property");
            mojo.setDoReanalyzeSession(true);
            mojo.execute();

            assertThat(mojo.getMavenProject().getProperties().getProperty("session-name-property"), is("example-session-name"));
            assertThat(mojo.getMavenProject().getProperties().getProperty("reanalyze-status-property"), is("true"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testStopRecordingWithException() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setMavenProject(new MavenProject());
            mojo.setProfileName("stop-recording-with-exception");
            mojo.execute();

            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));
        }
    }

    @Test
    public void testStopRecordingProperties() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setDoReanalyzeSession(true);
            mojo.setReanalyzeSessionTimeout(30000);
            mojo.setReanalyzeSessionPollingInterval(2500);
            mojo.setStopDelay(2500);
            mojo.setSessionNameProperty("abc");
            mojo.setReanalyzeStatusProperty("def");

            assertThat(mojo.isDoReanalyzeSession(), is(true));
            assertThat(mojo.getReanalyzeSessionTimeout(), is(30000));
            assertThat(mojo.getReanalyzeSessionPollingInterval(), is(2500));
            assertThat(mojo.getStopDelay(), is(2500));
            assertThat(mojo.getSessionNameProperty(), is("abc"));
            assertThat(mojo.getReanalyzeStatusProperty(), is("def"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }
}