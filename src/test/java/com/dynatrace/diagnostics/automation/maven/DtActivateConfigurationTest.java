package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
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
@PrepareForTest({SystemProfiles.class, DtActivateConfiguration.class})
public class DtActivateConfigurationTest {
    @Test
    public void activateConfigurationTest() throws Exception {
        DtActivateConfiguration activateConfiguration = spy(new DtActivateConfiguration());
        doReturn(null).when(activateConfiguration).getDynatraceClient();

        SystemProfiles systemProfiles = spy(new SystemProfiles(null));
        doReturn(true).when(systemProfiles).activateProfileConfiguration("profile", "config");
        doThrow(new ServerConnectionException("message", new Exception())).when(systemProfiles).activateProfileConfiguration("failedProfile", "config");

        whenNew(SystemProfiles.class).withAnyArguments().thenReturn(systemProfiles);

        activateConfiguration.setUsername("admin");
        activateConfiguration.setPassword("adminPassword");
        activateConfiguration.setIgnoreSSLErrors(true);
        activateConfiguration.setServerUrl("http://localhost:8080");

        try {
            activateConfiguration.setProfileName("profile");
            activateConfiguration.setConfiguration("config");
            activateConfiguration.execute();
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }

        try {
            activateConfiguration.setProfileName("failedProfile");
            activateConfiguration.setConfiguration("config");
            activateConfiguration.execute();
            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));
        }
    }


}