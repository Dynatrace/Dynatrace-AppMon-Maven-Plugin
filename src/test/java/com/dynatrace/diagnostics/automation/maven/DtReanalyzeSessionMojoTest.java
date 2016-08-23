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
@PrepareForTest({Sessions.class, DtReanalyzeSession.class})
public class DtReanalyzeSessionMojoTest extends AbstractDynatraceMojoTest<DtReanalyzeSession> {
    private static final String REANALYZE_SESSION_GOAL_NAME = "reanalyzeSession";

    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.applyFreshMojo();

        Sessions sessions = spy(new Sessions(mojo.getDynatraceClient()));

        /** define responses */
        doReturn(true).when(sessions).reanalyze("example-session-name");
        doReturn(true).when(sessions).getReanalysisStatus("example-session-name");

        doThrow(new ServerConnectionException("message", new Exception())).when(sessions).reanalyze("reanalyze-session-with-exception");

        whenNew(Sessions.class).withAnyArguments().thenReturn(sessions);

        /** verify default values */
        assertThat(mojo.getReanalyzeSessionTimeout(), is(60000));
        assertThat(mojo.getReanalyzeSessionPollingInterval(), is(5000));
    }

    @Override
    protected String getMojoGoalName() {
        return REANALYZE_SESSION_GOAL_NAME;
    }

    @Test
    public void testStopRecordingWithSuccess() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setMavenProject(new MavenProject());
            mojo.setSessionName("example-session-name");
            mojo.setReanalyzeStatusProperty("reanalyze-status-property");
            mojo.execute();

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
            mojo.setSessionName("reanalyze-session-with-exception");
            mojo.setReanalyzeStatusProperty("reanalyze-status-property");
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
            mojo.setReanalyzeSessionTimeout(30000);
            mojo.setReanalyzeSessionPollingInterval(2500);
            mojo.setReanalyzeStatusProperty("def");

            assertThat(mojo.getReanalyzeSessionTimeout(), is(30000));
            assertThat(mojo.getReanalyzeSessionPollingInterval(), is(2500));
            assertThat(mojo.getReanalyzeStatusProperty(), is("def"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }
}