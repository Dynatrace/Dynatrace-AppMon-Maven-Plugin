package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.agentsandcollectors.AgentsAndCollectors;
import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
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
@PrepareForTest({AgentsAndCollectors.class, DtRestartCollector.class})
public class DtRestartCollectorMojoTest extends AbstractDynatraceMojoTest<DtRestartCollector> {
    private static final String RESTART_COLLECTOR_GOAL_NAME = "restartCollector";

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.applyFreshMojo();

        AgentsAndCollectors agentsAndCollectors = spy(new AgentsAndCollectors(this.getMojo().getDynatraceClient()));

        /** define responses */
        doReturn(true).when(agentsAndCollectors).restartCollector("collector-restart-true");
        doReturn(true).when(agentsAndCollectors).shutdownCollector("collector-shutdown-true");
        doThrow(new ServerConnectionException("message", new Exception())).when(agentsAndCollectors).restartCollector("collector-restart-exception");
        doThrow(new ServerConnectionException("message", new Exception())).when(agentsAndCollectors).shutdownCollector("collector-shutdown-exception");

        whenNew(AgentsAndCollectors.class).withAnyArguments().thenReturn(agentsAndCollectors);

        /** verify default values */
        assertThat(this.getMojo().getRestart(), is(true));
    }

    @Override
    protected String getMojoGoalName() {
        return RESTART_COLLECTOR_GOAL_NAME;
    }

    @Test
    public void testRestartCollectorWithoutCollectorName() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setRestart(true);
            this.getMojo().execute();

            fail("Exception should be thrown - collector name is null");
        } catch (Exception e) {
            assertThat(e, instanceOf(NullPointerException.class));
        }
    }

    @Test
    public void testRestartCollectorWithSuccess() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setCollector("collector-restart-true");
            this.getMojo().setRestart(true);
            this.getMojo().execute();
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testShutdownCollectorWithSuccess() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setCollector("collector-shutdown-true");
            this.getMojo().setRestart(false);
            this.getMojo().execute();
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testRestartCollectorWithException() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setCollector("collector-restart-exception");
            this.getMojo().setRestart(true);
            this.getMojo().execute();
            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));
        }
    }

    @Test
    public void testShutdownCollectorWithException() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setCollector("collector-shutdown-exception");
            this.getMojo().setRestart(false);
            this.getMojo().execute();
            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));
        }
    }
}