package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.sessions.Sessions;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
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

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.applyFreshMojo();

        Sessions sessions = spy(new Sessions(this.getMojo().getDynatraceClient()));

        /** define responses */
        doReturn("example-session-name").when(sessions).stopRecording("stop-recording");
        doReturn(true).when(sessions).reanalyze("example-session-name");
        doReturn(true).when(sessions).getReanalysisStatus("example-session-name");

        doThrow(new ServerConnectionException("message", new Exception())).when(sessions).stopRecording("stop-recording-with-exception");

        whenNew(Sessions.class).withAnyArguments().thenReturn(sessions);

        /** verify default values */
        assertThat(this.getMojo().isDoReanalyzeSession(), is(false));
        assertThat(this.getMojo().getReanalyzeSessionTimeout(), is(60000));
        assertThat(this.getMojo().getReanalyzeSessionPollingInterval(), is(5000));
        assertThat(this.getMojo().getStopDelay(), is(0));
    }

    @Override
    protected String getMojoGoalName() {
        return STOP_RECORDING_GOAL_NAME;
    }

    @Test
    public void testStopRecordingWithSuccess() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setMavenProject(new MavenProject());
            this.getMojo().getMavenProject().getProperties().setProperty("dtSessionNameProperty", "other-session-name-property");
            this.getMojo().setSessionNameProperty("session-name-property");
            this.getMojo().setProfileName("stop-recording");
            this.getMojo().execute();

            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("session-name-property"), is("example-session-name"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }


    @Test
    public void testStopRecordingWithSessionNamePropertyFromProject() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setMavenProject(new MavenProject());
            this.getMojo().getMavenProject().getProperties().setProperty("dtSessionNameProperty", "session-name-property");
            this.getMojo().setProfileName("stop-recording");
            this.getMojo().execute();

            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("session-name-property"), is("example-session-name"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testStopRecordingWithReanalyzeSession() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setMavenProject(new MavenProject());
            this.getMojo().setProfileName("stop-recording");
            this.getMojo().setSessionNameProperty("session-name-property");
            this.getMojo().setReanalyzeStatusProperty("reanalyze-status-property");
            this.getMojo().setDoReanalyzeSession(true);
            this.getMojo().execute();

            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("session-name-property"), is("example-session-name"));
            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("reanalyze-status-property"), is("true"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testStopRecordingWithException() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setMavenProject(new MavenProject());
            this.getMojo().setProfileName("stop-recording-with-exception");
            this.getMojo().execute();

            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));
        }
    }

    @Test
    public void testStopRecordingProperties() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setDoReanalyzeSession(true);
            this.getMojo().setReanalyzeSessionTimeout(30000);
            this.getMojo().setReanalyzeSessionPollingInterval(2500);
            this.getMojo().setStopDelay(2500);
            this.getMojo().setSessionNameProperty("abc");
            this.getMojo().setReanalyzeStatusProperty("def");

            assertThat(this.getMojo().isDoReanalyzeSession(), is(true));
            assertThat(this.getMojo().getReanalyzeSessionTimeout(), is(30000));
            assertThat(this.getMojo().getReanalyzeSessionPollingInterval(), is(2500));
            assertThat(this.getMojo().getStopDelay(), is(2500));
            assertThat(this.getMojo().getSessionNameProperty(), is("abc"));
            assertThat(this.getMojo().getReanalyzeStatusProperty(), is("def"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }
}