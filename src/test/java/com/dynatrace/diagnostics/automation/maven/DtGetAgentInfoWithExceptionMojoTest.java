package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.agentsandcollectors.AgentsAndCollectors;
import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import org.apache.maven.plugin.MojoExecutionException;
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

    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.applyFreshMojo();

        AgentsAndCollectors agentsAndCollectors = spy(new AgentsAndCollectors(mojo.getDynatraceClient()));

        /** define responses */
        doThrow(new ServerConnectionException("message", new Exception())).when(agentsAndCollectors).fetchAgents();
        whenNew(AgentsAndCollectors.class).withAnyArguments().thenReturn(agentsAndCollectors);

        /** verify default values */
        assertThat(mojo.getInfoForAgentByIndex(), is(-1));
    }

    @Override
    protected String getMojoGoalName() {
        return GET_AGENT_INFO_GOAL_NAME;
    }

    @Test
    public void testGetAgentInfoByIndexWithException() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setInfoForAgentByIndex(0);

            mojo.setAgentCountProperty("agents-count");
            mojo.setAgentNameProperty("agent-name");
            mojo.setAgentHostNameProperty("agent-hostname");
            mojo.setAgentProcessIdProperty("agent-process-id");

            mojo.execute();

            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));
        }
    }
}
