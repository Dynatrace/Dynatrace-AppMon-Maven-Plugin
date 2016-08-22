package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.systemprofiles.SystemProfiles;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SystemProfiles.class, DtActivateConfiguration.class})
public class DtActivateConfigurationTest extends AbstractDynatraceMojoTest<DtActivateConfiguration> {
    private static final String ACTIVATE_CONFIGURATION_GOAL_NAME = "activateConfiguration";

    /** server sdk class used in tested mojo */
    private SystemProfiles systemProfiles;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.applyFreshMojo();

        systemProfiles = spy(new SystemProfiles(mojo.getDynatraceClient()));

        /** define responses */
        doReturn(true).when(systemProfiles).activateProfileConfiguration("profile", "config-true");
        doReturn(false).when(systemProfiles).activateProfileConfiguration("profile", "config-false");
        doThrow(new ServerConnectionException("message", new Exception())).when(systemProfiles).activateProfileConfiguration("profile", "config-exception");

        whenNew(SystemProfiles.class).withAnyArguments().thenReturn(systemProfiles);
    }

    @Override
    protected String getMojoGoalName() {
        return ACTIVATE_CONFIGURATION_GOAL_NAME;
    }

    @Test
    public void testActivateConfigurationSuccessTrue() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setProfileName("profile");
            mojo.setConfiguration("config-true");
            mojo.execute();
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testActivateConfigurationSuccessFalse() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setProfileName("profile");
            mojo.setConfiguration("config-false");
            mojo.execute();
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testActivateConfigurationWithException() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setProfileName("profile");
            mojo.setConfiguration("config-exception");
            mojo.execute();

            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));
        }
    }
}