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
@PrepareForTest({Sessions.class, DtReanalyzeSession.class})
public class DtReanalyzeSessionMojoTest extends AbstractDynatraceMojoTest<DtReanalyzeSession> {
    private static final String REANALYZE_SESSION_GOAL_NAME = "reanalyzeSession";

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.applyFreshMojo();

        Sessions sessions = spy(new Sessions(this.getMojo().getDynatraceClient()));

        /** define responses */
        doReturn(true).when(sessions).reanalyze("example-session-name");
        doReturn(true).when(sessions).getReanalysisStatus("example-session-name");

        doThrow(new ServerConnectionException("message", new Exception())).when(sessions).reanalyze("reanalyze-session-with-exception");

        whenNew(Sessions.class).withAnyArguments().thenReturn(sessions);

        /** verify default values */
        assertThat(this.getMojo().getReanalyzeSessionTimeout(), is(60000));
        assertThat(this.getMojo().getReanalyzeSessionPollingInterval(), is(5000));
    }

    @Override
    protected String getMojoGoalName() {
        return REANALYZE_SESSION_GOAL_NAME;
    }

    @Test
    public void testReanalyzeSessionWithSuccess() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setMavenProject(new MavenProject());
            this.getMojo().setSessionName("example-session-name");
            this.getMojo().setReanalyzeStatusProperty("reanalyze-status-property");
            this.getMojo().execute();

            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("reanalyze-status-property"), is("true"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testReanalyzeSessionWithException() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setMavenProject(new MavenProject());
            this.getMojo().setSessionName("reanalyze-session-with-exception");
            this.getMojo().setReanalyzeStatusProperty("reanalyze-status-property");
            this.getMojo().execute();

            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));
        }
    }


    @Test
    public void testReanalyzeSessionProperties() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setReanalyzeSessionTimeout(30000);
            this.getMojo().setReanalyzeSessionPollingInterval(2500);
            this.getMojo().setReanalyzeStatusProperty("def");

            assertThat(this.getMojo().getReanalyzeSessionTimeout(), is(30000));
            assertThat(this.getMojo().getReanalyzeSessionPollingInterval(), is(2500));
            assertThat(this.getMojo().getReanalyzeStatusProperty(), is("def"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }
}