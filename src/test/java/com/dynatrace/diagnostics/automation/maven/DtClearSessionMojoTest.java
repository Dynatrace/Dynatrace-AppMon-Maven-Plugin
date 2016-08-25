package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.sessions.Sessions;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Sessions.class, DtClearSession.class})
public class DtClearSessionMojoTest extends AbstractDynatraceMojoTest<DtClearSession> {
    private static final String CLEAR_SESSION_GOAL_NAME = "clearSession";

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.applyFreshMojo();

        Sessions sessions = spy(new Sessions(this.getMojo().getDynatraceClient()));

        /** define responses */
        doReturn(true).when(sessions).clear("profile-success");
        doThrow(new ServerConnectionException("message", new Exception())).when(sessions).clear("profile-fail");

        whenNew(Sessions.class).withAnyArguments().thenReturn(sessions);
    }

    @Override
    protected String getMojoGoalName() {
        return CLEAR_SESSION_GOAL_NAME;
    }

    @Test
    public void testClearSessionWithSuccess() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setProfileName("profile-success");
            this.getMojo().execute();
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testClearSessionWithException() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setProfileName("profile-fail");
            this.getMojo().execute();
            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));
        }
    }
}