package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.agentsandcollectors.AgentsAndCollectors;
import com.dynatrace.sdk.server.agentsandcollectors.models.AgentInformation;
import com.dynatrace.sdk.server.agentsandcollectors.models.Agents;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
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

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.applyFreshMojo();

        AgentsAndCollectors agentsAndCollectors = spy(new AgentsAndCollectors(this.getMojo().getDynatraceClient()));

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
        assertThat(this.getMojo().getInfoForAgentByIndex(), is(-1));
    }

    @Override
    protected String getMojoGoalName() {
        return GET_AGENT_INFO_GOAL_NAME;
    }

    @Test
    public void testGetAgentInfoByIndex() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setInfoForAgentByIndex(0);

            this.getMojo().setAgentCountProperty("agents-count");
            this.getMojo().setAgentNameProperty("agent-name");
            this.getMojo().setAgentHostNameProperty("agent-hostname");
            this.getMojo().setAgentProcessIdProperty("agent-process-id");

            this.getMojo().execute();

            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("agents-count"), is("2"));
            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("agent-name"), is("custom-name"));
            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("agent-hostname"), is("custom-host"));
            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("agent-process-id"), is("1234"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testGetAgentInfoByName() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setInfoForAgentByName("custom-name-2");

            this.getMojo().setAgentCountProperty("agents-count");
            this.getMojo().setAgentNameProperty("agent-name");
            this.getMojo().setAgentHostNameProperty("agent-hostname");
            this.getMojo().setAgentProcessIdProperty("agent-process-id");

            this.getMojo().execute();

            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("agents-count"), is("2"));
            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("agent-name"), is("custom-name-2"));
            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("agent-hostname"), is("custom-host-2"));
            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("agent-process-id"), is("5678"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testGetAgentInfoByIndexNotFound() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setInfoForAgentByIndex(5);

            this.getMojo().setAgentCountProperty("agents-count");
            this.getMojo().setAgentNameProperty("agent-name");
            this.getMojo().setAgentHostNameProperty("agent-hostname");
            this.getMojo().setAgentProcessIdProperty("agent-process-id");

            this.getMojo().execute();

            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("agents-count"), is("2"));

            assertNull(this.getMojo().getMavenProject().getProperties().getProperty("agent-name"));
            assertNull(this.getMojo().getMavenProject().getProperties().getProperty("agent-hostname"));
            assertNull(this.getMojo().getMavenProject().getProperties().getProperty("agent-process-id"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testGetAgentInfoByIndexAndName() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setInfoForAgentByIndex(0);
            this.getMojo().setInfoForAgentByName("custom-name-2");

            this.getMojo().setAgentCountProperty("agents-count");
            this.getMojo().setAgentNameProperty("agent-name");
            this.getMojo().setAgentHostNameProperty("agent-hostname");
            this.getMojo().setAgentProcessIdProperty("agent-process-id");

            this.getMojo().execute();

            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("agents-count"), is("2"));
            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("agent-name"), is("custom-name-2"));
            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("agent-hostname"), is("custom-host-2"));
            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("agent-process-id"), is("5678"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testGetAgentInfoByIndexWithoutProperties() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setInfoForAgentByIndex(0);
            this.getMojo().execute();

            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(NullPointerException.class));
        }
    }

    @Test
    public void testGetAgentInfoByNameWithoutProperties() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setInfoForAgentByName("custom-name");
            this.getMojo().execute();

            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(NullPointerException.class));
        }
    }


    @Test
    public void testGetAgentInfoProperties() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setMavenProject(new MavenProject());

            this.getMojo().setAgentCountProperty("agents-count");
            this.getMojo().setAgentNameProperty("agent-name");
            this.getMojo().setAgentHostNameProperty("agent-hostname");
            this.getMojo().setAgentProcessIdProperty("process-id");
            this.getMojo().setInfoForAgentByIndex(5);
            this.getMojo().setInfoForAgentByName("agent-name-info");

            assertThat(this.getMojo().getAgentCountProperty(), is("agents-count"));
            assertThat(this.getMojo().getAgentNameProperty(), is("agent-name"));
            assertThat(this.getMojo().getAgentHostNameProperty(), is("agent-hostname"));
            assertThat(this.getMojo().getAgentProcessIdProperty(), is("process-id"));
            assertThat(this.getMojo().getInfoForAgentByIndex(), is(5));
            assertThat(this.getMojo().getInfoForAgentByName(), is("agent-name-info"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }


}
