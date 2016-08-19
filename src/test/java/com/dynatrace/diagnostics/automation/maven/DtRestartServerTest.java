package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.servermanagement.ServerManagement;
import com.dynatrace.sdk.server.sessions.Sessions;
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
@PrepareForTest({ServerManagement.class, DtRestartServer.class})
public class DtRestartServerTest {
    @Test
    public void restartServerTest() throws Exception {
        DtRestartServer restartServer = spy(new DtRestartServer());
        doReturn(null).when(restartServer).getDynatraceClient();

        ServerManagement sessions = spy(new ServerManagement(null));
        doReturn(true).when(sessions).restart();
        doThrow(new ServerConnectionException("message", new Exception())).when(sessions).shutdown();
        whenNew(ServerManagement.class).withAnyArguments().thenReturn(sessions);

        /* restart successful */
        try {
            restartServer.execute();
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }

        /* shutdown with fail */
        restartServer.setRestart(false);

        try {
            restartServer.execute();
            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));
        }
    }
}