package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.exceptions.ServerResponseException;
import com.dynatrace.sdk.server.systemprofiles.SystemProfiles;
import org.apache.maven.plugin.MojoExecutionException;
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
@PrepareForTest({SystemProfiles.class, DtEnableProfile.class})
public class DtEnableProfileMojoTest extends AbstractDynatraceMojoTest<DtEnableProfile> {
    private static final String ENABLE_PROFILE_GOAL_NAME = "enableProfile";

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.applyFreshMojo();

        SystemProfiles systemProfiles = spy(new SystemProfiles(this.getMojo().getDynatraceClient()));

        /** define responses */
        doReturn(true).when(systemProfiles).enableProfile("enable-success-true");
        doReturn(false).when(systemProfiles).enableProfile("enable-success-false");
        doReturn(true).when(systemProfiles).disableProfile("disable-success-true");
        doReturn(false).when(systemProfiles).disableProfile("disable-success-false");

        doThrow(new ServerConnectionException("message", new Exception())).when(systemProfiles).enableProfile("enable-exception");
        doThrow(new ServerResponseException(500, "message", new Exception())).when(systemProfiles).disableProfile("disable-exception");

        whenNew(SystemProfiles.class).withAnyArguments().thenReturn(systemProfiles);

        /** verify default values */
        assertThat(this.getMojo().isEnable(), is(true));
    }

    @Override
    protected String getMojoGoalName() {
        return ENABLE_PROFILE_GOAL_NAME;
    }

    @Test
    public void testEnableTrueSuccessTrue() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setProfileName("enable-success-true");
            this.getMojo().setEnable(true);
            this.getMojo().execute();
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testEnableTrueSuccessFalse() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setProfileName("enable-success-false");
            this.getMojo().setEnable(true);
            this.getMojo().execute();
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testEnableFalseSuccessTrue() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setProfileName("disable-success-true");
            this.getMojo().setEnable(false);
            this.getMojo().execute();
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testEnableFalseSuccessFalse() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setProfileName("disable-success-false");
            this.getMojo().setEnable(false);
            this.getMojo().execute();
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testEnableTrueWithException() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setProfileName("enable-exception");
            this.getMojo().setEnable(true);
            this.getMojo().execute();

            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));
        }
    }

    @Test
    public void testEnableFalseWithException() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setProfileName("disable-exception");
            this.getMojo().setEnable(false);
            this.getMojo().execute();

            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));

            /** maybe exception message should be more verbose? */
            //assertThat(e.getMessage(), containsString("500"));
        }
    }
}