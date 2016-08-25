package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.exceptions.ServerResponseException;
import com.dynatrace.sdk.server.memorydumps.MemoryDumps;
import com.dynatrace.sdk.server.memorydumps.models.JobState;
import com.dynatrace.sdk.server.memorydumps.models.MemoryDumpJob;
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
@PrepareForTest({MemoryDumps.class, DtMemoryDump.class})
public class DtMemoryDumpMojoTest extends AbstractDynatraceMojoTest<DtMemoryDump> {
    private static final String MEMORY_DUMP_MOJO_NAME = "memoryDump";

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.applyFreshMojo();

        MemoryDumps memoryDumps = spy(new MemoryDumps(this.getMojo().getDynatraceClient()));

        /** define responses */
        doReturn("https://localhost:8021/rest/management/profiles/system-profile-success/memorydumpjobs/Memory%20Dump%20%5B11880540745601%5D")
                .when(memoryDumps).createMemoryDumpJob(Mockito.anyString(), Mockito.any(MemoryDumpJob.class));

        MemoryDumpJob memoryDumpJob = new MemoryDumpJob();
        memoryDumpJob.setState(JobState.FINISHED);

        MemoryDumpJob memoryDumpJob2 = new MemoryDumpJob();
        memoryDumpJob2.setState(JobState.RUNNING);

        doReturn(memoryDumpJob).when(memoryDumps).getMemoryDumpJob("system-profile-success", "Memory Dump [11880540745601]");
        doReturn(memoryDumpJob2).when(memoryDumps).getMemoryDumpJob("system-profile-timeout", "Memory Dump [11880540745601]");
        doThrow(new ServerConnectionException("message", new Exception())).when(memoryDumps).getMemoryDumpJob("system-profile-exception", "Memory Dump [11880540745601]");
        doThrow(new ServerResponseException(500, "message", new Exception())).when(memoryDumps).getMemoryDumpJob("system-profile-exception-continue", "Memory Dump [11880540745601]");

        whenNew(MemoryDumps.class).withAnyArguments().thenReturn(memoryDumps);

        /** verify default values */
        assertThat(this.getMojo().getDumpType(), is("memdump_simple"));
        assertThat(this.getMojo().isSessionLocked(), is(true));
        assertThat(this.getMojo().getDoGc(), is(false));
        assertThat(this.getMojo().getAutoPostProcess(), is(false));
        assertThat(this.getMojo().getCapturePrimitives(), is(false));
        assertThat(this.getMojo().getCaptureStrings(), is(false));
        assertThat(this.getMojo().getWaitForDumpTimeout(), is(60000));
        assertThat(this.getMojo().getWaitForDumpPollingInterval(), is(5000));
    }

    @Override
    protected String getMojoGoalName() {
        return MEMORY_DUMP_MOJO_NAME;
    }

    @Test
    public void testMemoryDumpWithSuccess() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setProfileName("system-profile-success");
            this.getMojo().setAgentName("agent-name");
            this.getMojo().setHostName("host-name");
            this.getMojo().setProcessId(1234);

            this.getMojo().setMemoryDumpNameProperty("dump-name");
            this.getMojo().setDumpStatusProperty("dump-status");

            this.getMojo().execute();

            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("dump-name"), is("Memory Dump [11880540745601]"));
            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("dump-status"), is("true"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testMemoryDumpWithTimeout() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setProfileName("system-profile-timeout");
            this.getMojo().setAgentName("agent-name");
            this.getMojo().setHostName("host-name");
            this.getMojo().setProcessId(1234);

            this.getMojo().setMemoryDumpNameProperty("dump-name");
            this.getMojo().setDumpStatusProperty("dump-status");

            this.getMojo().setWaitForDumpTimeout(100);
            this.getMojo().setWaitForDumpPollingInterval(10);

            this.getMojo().execute();

            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("dump-name"), is("Memory Dump [11880540745601]"));
            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("dump-status"), is("false"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testMemoryDumpWithException() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setProfileName("system-profile-exception");
            this.getMojo().setAgentName("agent-name");
            this.getMojo().setHostName("host-name");
            this.getMojo().setProcessId(1234);

            this.getMojo().setMemoryDumpNameProperty("dump-name");
            this.getMojo().setDumpStatusProperty("dump-status");

            this.getMojo().setWaitForDumpTimeout(100);
            this.getMojo().setWaitForDumpPollingInterval(10);

            this.getMojo().execute();

            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));
        }
    }

    @Test
    public void testMemoryDumpWithExceptionContinue() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setProfileName("system-profile-exception-continue");
            this.getMojo().setAgentName("agent-name");
            this.getMojo().setHostName("host-name");
            this.getMojo().setProcessId(1234);

            this.getMojo().setMemoryDumpNameProperty("dump-name");
            this.getMojo().setDumpStatusProperty("dump-status");

            this.getMojo().setWaitForDumpTimeout(100);
            this.getMojo().setWaitForDumpPollingInterval(10);

            this.getMojo().execute();

            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("dump-name"), is("Memory Dump [11880540745601]"));
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
}