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
@PrepareForTest({AgentsAndCollectors.class, DtGetAgentInfo.class})
public class DtGetAgentInfoWithExceptionMojoTest extends AbstractDynatraceMojoTest<DtGetAgentInfo> {
    private static final String GET_AGENT_INFO_GOAL_NAME = "getAgentInfo";

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.applyFreshMojo();

        AgentsAndCollectors agentsAndCollectors = spy(new AgentsAndCollectors(this.getMojo().getDynatraceClient()));

        /** define responses */
        doThrow(new ServerConnectionException("message", new Exception())).when(agentsAndCollectors).fetchAgents();
        whenNew(AgentsAndCollectors.class).withAnyArguments().thenReturn(agentsAndCollectors);

        /** verify default values */
        assertThat(this.getMojo().getInfoForAgentByIndex(), is(-1));
    }

    @Override
    protected String getMojoGoalName() {
        return GET_AGENT_INFO_GOAL_NAME;
    }

    @Test
    public void testGetAgentInfoByIndexWithException() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setInfoForAgentByIndex(0);

            this.getMojo().setAgentCountProperty("agents-count");
            this.getMojo().setAgentNameProperty("agent-name");
            this.getMojo().setAgentHostNameProperty("agent-hostname");
            this.getMojo().setAgentProcessIdProperty("agent-process-id");

            this.getMojo().execute();

            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));
        }
    }
}
