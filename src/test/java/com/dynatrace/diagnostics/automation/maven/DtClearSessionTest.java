package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.sessions.Sessions;
import com.dynatrace.sdk.server.systemprofiles.SystemProfiles;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Sessions.class, DtClearSession.class})
public class DtClearSessionTest {
    @Test
    public void clearSessionTest() throws Exception {
        DtClearSession clearSession = spy(new DtClearSession());
        doReturn(null).when(clearSession).getDynatraceClient();

        Sessions sessions = spy(new Sessions(null));
        doReturn(true).when(sessions).clear("success");
        doThrow(new ServerConnectionException("message", new Exception())).when(sessions).clear("fail");

        whenNew(Sessions.class).withAnyArguments().thenReturn(sessions);

        /* successful clearing */
        try {
            clearSession.setProfileName("success");
            clearSession.execute();
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }

        /* failed clearing */
        try {
            clearSession.setProfileName("fail");
            clearSession.execute();
            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));
        }
    }
}