package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.agentsandcollectors.AgentsAndCollectors;
import com.dynatrace.sdk.server.agentsandcollectors.models.AgentInformation;
import com.dynatrace.sdk.server.agentsandcollectors.models.Agents;
import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.exceptions.ServerResponseException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;
import java.util.Properties;

@Mojo(name = "getAgentInfo")
@Execute(phase = LifecyclePhase.VERIFY)
public class DtGetAgentInfo extends DtServerBase {

	@Parameter(property = "dynaTrace.agentCountProperty")
	private String agentCountProperty;

	@Parameter(property = "dynaTrace.agentNameProperty")
	private String agentNameProperty;

	@Parameter(property = "dynaTrace.agentHostNameProperty")
	private String agentHostNameProperty;

	@Parameter(property = "dynaTrace.agentProcessIdProperty")
	private String agentProcessIdProperty;

	@Parameter(property = "dynaTrace.infoForAgentByIndex", defaultValue = "-1")
	private int infoForAgentByIndex = -1;

	@Parameter(property = "dynaTrace.infoForAgentByName")
	private String infoForAgentByName;

	@Override
	public void execute() throws MojoExecutionException {
		getLog().info("Execute with " + agentCountProperty + " " + getUsername() + " " + getPassword() + " " + getServerUrl()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$


		try {
			AgentsAndCollectors agentsAndCollectors = new AgentsAndCollectors(this.getDynatraceClient());
			Agents agentsContainer = agentsAndCollectors.fetchAgents();
			List<AgentInformation> agents = agentsContainer.getAgents();

			if(agentCountProperty != null && agentCountProperty.length() > 0) {
				getLog().info("Set " + agentCountProperty + " to " + String.valueOf(agents.size())); //$NON-NLS-1$ //$NON-NLS-2$
				mavenProject.getProperties().setProperty(agentCountProperty, String.valueOf(agents.size()));
			}

			AgentInformation agentForInfo = null;
			if (infoForAgentByIndex >= 0 && infoForAgentByIndex < agents.size()) {
				agentForInfo = agents.get(infoForAgentByIndex);
			}
			if (infoForAgentByName != null) {
				for (AgentInformation agent : agents) {
					if (agent.getName().equalsIgnoreCase(infoForAgentByName))
						agentForInfo = agent;
				}
			}

			if(agentForInfo != null) {
				getLog().info("Return agent info: " + agentForInfo.getName() + "/" + agentForInfo.getHost() + "/" + agentForInfo.getProcessId()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				Properties props = mavenProject.getProperties();
				props.setProperty(getAgentNameProperty(), agentForInfo.getName());
				props.setProperty(getAgentHostNameProperty(), agentForInfo.getHost());
				props.setProperty(getAgentProcessIdProperty(), String.valueOf(agentForInfo.getProcessId()));
			}

		} catch (ServerConnectionException | ServerResponseException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	public void setAgentCountProperty(String agentCountProperty) {
		this.agentCountProperty = agentCountProperty;
	}

	public String getAgentCountProperty() {
		return agentCountProperty;
	}

	public void setInfoForAgentByIndex(int infoForAgentByIndex) {
		this.infoForAgentByIndex = infoForAgentByIndex;
	}

	public int getInfoForAgentByIndex() {
		return infoForAgentByIndex;
	}

	public void setInfoForAgentByName(String infoForAgentByName) {
		this.infoForAgentByName = infoForAgentByName;
	}

	public String getInfoForAgentByName() {
		return infoForAgentByName;
	}

	public void setAgentNameProperty(String agentNameProperty) {
		this.agentNameProperty = agentNameProperty;
	}

	public String getAgentNameProperty() {
		return agentNameProperty;
	}

	public void setAgentHostNameProperty(String agentHostNameProperty) {
		this.agentHostNameProperty = agentHostNameProperty;
	}

	public String getAgentHostNameProperty() {
		return agentHostNameProperty;
	}

	public void setAgentProcessIdProperty(String agentProcessIdProperty) {
		this.agentProcessIdProperty = agentProcessIdProperty;
	}

	public String getAgentProcessIdProperty() {
		return agentProcessIdProperty;
	}
}
