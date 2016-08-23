package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.agentsandcollectors.AgentsAndCollectors;
import com.dynatrace.sdk.server.agentsandcollectors.models.AgentInformation;
import com.dynatrace.sdk.server.agentsandcollectors.models.Agents;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Agents.class, AgentInformation.class, AgentsAndCollectors.class, DtGetAgentInfo.class})
public class DtGetAgentInfoMojoTest extends AbstractDynatraceMojoTest<DtGetAgentInfo> {
    private static final String GET_AGENT_INFO_GOAL_NAME = "getAgentInfo";

    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.applyFreshMojo();

        AgentsAndCollectors agentsAndCollectors = spy(new AgentsAndCollectors(mojo.getDynatraceClient()));

        AgentInformation agentInformation = spy(new AgentInformation());
        doReturn("custom-name").when(agentInformation).getName();
        doReturn("custom-host").when(agentInformation).getHost();
        doReturn(1234).when(agentInformation).getProcessId();

        AgentInformation agentInformation2 = spy(new AgentInformation());
        doReturn("custom-name-2").when(agentInformation2).getName();
        doReturn("custom-host-2").when(agentInformation2).getHost();
        doReturn(5678).when(agentInformation2).getProcessId();

        ArrayList<AgentInformation> agentInformationList = new ArrayList<>();
        agentInformationList.add(agentInformation);
        agentInformationList.add(agentInformation2);

        Agents agents = spy(new Agents());
        doReturn(agentInformationList).when(agents).getAgents();

        /** define responses */
        doReturn(agents).when(agentsAndCollectors).fetchAgents();

        whenNew(AgentsAndCollectors.class).withAnyArguments().thenReturn(agentsAndCollectors);

        /** verify default values */
        assertThat(mojo.getInfoForAgentByIndex(), is(-1));
    }

    @Override
    protected String getMojoGoalName() {
        return GET_AGENT_INFO_GOAL_NAME;
    }

    @Test
    public void testGetAgentInfoByIndex() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setInfoForAgentByIndex(0);

            mojo.setAgentCountProperty("agents-count");
            mojo.setAgentNameProperty("agent-name");
            mojo.setAgentHostNameProperty("agent-hostname");
            mojo.setAgentProcessIdProperty("agent-process-id");

            mojo.execute();

            assertThat(mojo.getMavenProject().getProperties().getProperty("agents-count"), is("2"));
            assertThat(mojo.getMavenProject().getProperties().getProperty("agent-name"), is("custom-name"));
            assertThat(mojo.getMavenProject().getProperties().getProperty("agent-hostname"), is("custom-host"));
            assertThat(mojo.getMavenProject().getProperties().getProperty("agent-process-id"), is("1234"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testGetAgentInfoByName() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setInfoForAgentByName("custom-name-2");

            mojo.setAgentCountProperty("agents-count");
            mojo.setAgentNameProperty("agent-name");
            mojo.setAgentHostNameProperty("agent-hostname");
            mojo.setAgentProcessIdProperty("agent-process-id");

            mojo.execute();

            assertThat(mojo.getMavenProject().getProperties().getProperty("agents-count"), is("2"));
            assertThat(mojo.getMavenProject().getProperties().getProperty("agent-name"), is("custom-name-2"));
            assertThat(mojo.getMavenProject().getProperties().getProperty("agent-hostname"), is("custom-host-2"));
            assertThat(mojo.getMavenProject().getProperties().getProperty("agent-process-id"), is("5678"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testGetAgentInfoByIndexNotFound() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setInfoForAgentByIndex(5);

            mojo.setAgentCountProperty("agents-count");
            mojo.setAgentNameProperty("agent-name");
            mojo.setAgentHostNameProperty("agent-hostname");
            mojo.setAgentProcessIdProperty("agent-process-id");

            mojo.execute();

            assertThat(mojo.getMavenProject().getProperties().getProperty("agents-count"), is("2"));

            assertNull(mojo.getMavenProject().getProperties().getProperty("agent-name"));
            assertNull(mojo.getMavenProject().getProperties().getProperty("agent-hostname"));
            assertNull(mojo.getMavenProject().getProperties().getProperty("agent-process-id"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testGetAgentInfoByIndexAndName() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setInfoForAgentByIndex(0);
            mojo.setInfoForAgentByName("custom-name-2");

            mojo.setAgentCountProperty("agents-count");
            mojo.setAgentNameProperty("agent-name");
            mojo.setAgentHostNameProperty("agent-hostname");
            mojo.setAgentProcessIdProperty("agent-process-id");

            mojo.execute();

            assertThat(mojo.getMavenProject().getProperties().getProperty("agents-count"), is("2"));
            assertThat(mojo.getMavenProject().getProperties().getProperty("agent-name"), is("custom-name-2"));
            assertThat(mojo.getMavenProject().getProperties().getProperty("agent-hostname"), is("custom-host-2"));
            assertThat(mojo.getMavenProject().getProperties().getProperty("agent-process-id"), is("5678"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testGetAgentInfoByIndexWithoutProperties() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setInfoForAgentByIndex(0);
            mojo.execute();

            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(NullPointerException.class));
        }
    }

    @Test
    public void testGetAgentInfoByNameWithoutProperties() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setInfoForAgentByName("custom-name");
            mojo.execute();

            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(NullPointerException.class));
        }
    }


    @Test
    public void testGetAgentInfoProperties() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setMavenProject(new MavenProject());

            mojo.setAgentCountProperty("agents-count");
            mojo.setAgentNameProperty("agent-name");
            mojo.setAgentHostNameProperty("agent-hostname");
            mojo.setAgentProcessIdProperty("process-id");
            mojo.setInfoForAgentByIndex(5);
            mojo.setInfoForAgentByName("agent-name-info");

            assertThat(mojo.getAgentCountProperty(), is("agents-count"));
            assertThat(mojo.getAgentNameProperty(), is("agent-name"));
            assertThat(mojo.getAgentHostNameProperty(), is("agent-hostname"));
            assertThat(mojo.getAgentProcessIdProperty(), is("process-id"));
            assertThat(mojo.getInfoForAgentByIndex(), is(5));
            assertThat(mojo.getInfoForAgentByName(), is("agent-name-info"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }


}
