package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.resourcedumps.ResourceDumps;
import com.dynatrace.sdk.server.resourcedumps.models.CreateThreadDumpRequest;
import com.dynatrace.sdk.server.resourcedumps.models.ThreadDumpStatus;
import org.apache.maven.project.MavenProject;
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
public class DtThreadDumpMojoTest extends AbstractDynatraceMojoTest<DtThreadDump> {
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

        doReturn("thread-dump-schedule-id").when(resourceDumps).createThreadDump(Mockito.any(CreateThreadDumpRequest.class));
        doReturn(threadDumpStatus).when(resourceDumps).getThreadDumpStatus("some-profile", "thread-dump-schedule-id");

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
    public void testThreadDump() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setProfileName("some-profile");
            this.getMojo().setAgentName("agent-name");
            this.getMojo().setHostName("host-name");
            this.getMojo().setProcessId(1234);

            this.getMojo().setDumpStatusProperty("dump-status");
            this.getMojo().setThreadDumpNameProperty("dump-name");

            this.getMojo().execute();

            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("dump-name"), is("thread-dump-schedule-id"));
            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("dump-status"), is("true"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testThreadDumpWithoutProperties() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().execute();

            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(NullPointerException.class));
        }
    }

    @Test
    public void testStartTestProperties() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setMavenProject(new MavenProject());

            this.getMojo().setSessionLocked(true);
            this.getMojo().setThreadDumpNameProperty("dump-name-property");
            this.getMojo().setDumpStatusProperty("dump-status-property");
            this.getMojo().setWaitForDumpTimeout(30000);
            this.getMojo().setWaitForDumpPollingInterval(2500);

            assertThat(this.getMojo().isSessionLocked(), is(true));
            assertThat(this.getMojo().getThreadDumpNameProperty(), is("dump-name-property"));
            assertThat(this.getMojo().getDumpStatusProperty(), is("dump-status-property"));
            assertThat(this.getMojo().getWaitForDumpTimeout(), is(30000));
            assertThat(this.getMojo().getWaitForDumpPollingInterval(), is(2500));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

}