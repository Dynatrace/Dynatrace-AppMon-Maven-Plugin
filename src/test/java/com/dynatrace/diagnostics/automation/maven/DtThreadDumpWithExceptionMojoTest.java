package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.resourcedumps.ResourceDumps;
import com.dynatrace.sdk.server.resourcedumps.models.CreateThreadDumpRequest;
import com.dynatrace.sdk.server.resourcedumps.models.ThreadDumpStatus;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ThreadDumpStatus.class, ResourceDumps.class, DtThreadDump.class})
public class DtThreadDumpWithExceptionMojoTest extends AbstractDynatraceMojoTest<DtThreadDump> {
    private static final String THREAD_DUMP_MOJO_NAME = "threadDump";

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.applyFreshMojo();

        ResourceDumps resourceDumps = spy(new ResourceDumps(this.getMojo().getDynatraceClient()));

        /** define responses */
        ThreadDumpStatus threadDumpStatus = spy(new ThreadDumpStatus());
        doReturn(true).when(threadDumpStatus).getResultValue();

        doThrow(new ServerConnectionException("message", new Exception())).when(resourceDumps).createThreadDump(Mockito.any(CreateThreadDumpRequest.class));

        whenNew(ResourceDumps.class).withAnyArguments().thenReturn(resourceDumps);

        /** verify default values */
        assertThat(this.getMojo().getWaitForDumpTimeout(), is(60000));
        assertThat(this.getMojo().getWaitForDumpPollingInterval(), is(5000));
    }

    @Override
    protected String getMojoGoalName() {
        return THREAD_DUMP_MOJO_NAME;
    }


    @Test
    public void testThreadDumpWithException() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setProfileName("some-profile");
            this.getMojo().setAgentName("agent-name");
            this.getMojo().setHostName("host-name");
            this.getMojo().setProcessId(1234);

            this.getMojo().execute();

            fail("Exception should be thrown - build version is not set");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));
        }
    }


}