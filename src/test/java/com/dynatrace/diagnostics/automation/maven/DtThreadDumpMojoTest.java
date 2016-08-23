package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.resourcedumps.ResourceDumps;
import com.dynatrace.sdk.server.resourcedumps.models.CreateThreadDumpRequest;
import com.dynatrace.sdk.server.resourcedumps.models.ThreadDumpStatus;
import org.apache.maven.project.MavenProject;
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
public class DtThreadDumpMojoTest extends AbstractDynatraceMojoTest<DtThreadDump> {
    private static final String THREAD_DUMP_MOJO_NAME = "threadDump";

    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.applyFreshMojo();

        ResourceDumps resourceDumps = spy(new ResourceDumps(mojo.getDynatraceClient()));


        /** define responses */
        ThreadDumpStatus threadDumpStatus = spy(new ThreadDumpStatus());
        doReturn(true).when(threadDumpStatus).getResultValue();

        doReturn("thread-dump-schedule-id").when(resourceDumps).createThreadDump(Mockito.any(CreateThreadDumpRequest.class));
        doReturn(threadDumpStatus).when(resourceDumps).getThreadDumpStatus("some-profile", "thread-dump-schedule-id");

        whenNew(ResourceDumps.class).withAnyArguments().thenReturn(resourceDumps);

        /** verify default values */
        assertThat(mojo.getWaitForDumpTimeout(), is(60000));
        assertThat(mojo.getWaitForDumpPollingInterval(), is(5000));
    }

    @Override
    protected String getMojoGoalName() {
        return THREAD_DUMP_MOJO_NAME;
    }


    @Test
    public void testThreadDump() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setProfileName("some-profile");
            mojo.setAgentName("agent-name");
            mojo.setHostName("host-name");
            mojo.setProcessId(1234);

            mojo.setDumpStatusProperty("dump-status");
            mojo.setThreadDumpNameProperty("dump-name");

            mojo.execute();

            assertThat(mojo.getMavenProject().getProperties().getProperty("dump-name"), is("thread-dump-schedule-id"));
            assertThat(mojo.getMavenProject().getProperties().getProperty("dump-status"), is("true"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testThreadDumpWithoutProperties() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.execute();

            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(NullPointerException.class));
        }
    }

    @Test
    public void testStartTestProperties() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setMavenProject(new MavenProject());

            mojo.setSessionLocked(true);
            mojo.setThreadDumpNameProperty("dump-name-property");
            mojo.setDumpStatusProperty("dump-status-property");
            mojo.setWaitForDumpTimeout(30000);
            mojo.setWaitForDumpPollingInterval(2500);

            assertThat(mojo.isSessionLocked(), is(true));
            assertThat(mojo.getThreadDumpNameProperty(), is("dump-name-property"));
            assertThat(mojo.getDumpStatusProperty(), is("dump-status-property"));
            assertThat(mojo.getWaitForDumpTimeout(), is(30000));
            assertThat(mojo.getWaitForDumpPollingInterval(), is(2500));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

}