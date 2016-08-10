package com.dynatrace.diagnostics.automation.maven;

import java.util.ArrayList;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;

import com.dynatrace.diagnostics.automation.rest.sdk.entity.Agent;

/**
 * @goal getAgentInfo
 * @phase verify
 */
public class DtGetAgentInfo extends DtServerBase {

	/**
	 * @parameter expression="${dynaTrace.agentCountProperty}"
	 */
	private String agentCountProperty;

	/**
	 * @parameter expression="${dynaTrace.agentNameProperty}"
	 */
	private String agentNameProperty;

	/**
	 * @parameter expression="${dynaTrace.agentHostNameProperty}"
	 */
	private String agentHostNameProperty;

	/**
	 * @parameter expression="${dynaTrace.agentProcessIdProperty}"
	 */
	private String agentProcessIdProperty;

	/**
	 * @parameter expression="${dynaTrace.infoForAgentByIndex}" default-value="-1"
	 */
	private int infoForAgentByIndex = -1;

	/**
	 * @parameter expression="${dynaTrace.infoForAgentByName}"
	 */
	private String infoForAgentByName;

	@Override
	public void execute() throws MojoExecutionException {
		getLog().info("Execute with " + agentCountProperty + " " + getUsername() + " " + getPassword() + " " + getServerUrl()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		ArrayList<Agent> agents = getEndpoint().getAgents();
		if(agentCountProperty != null && agentCountProperty.length() > 0) {
			getLog().info("Set " + agentCountProperty + " to " + String.valueOf(agents.size())); //$NON-NLS-1$ //$NON-NLS-2$
			mavenProject.getProperties().setProperty(agentCountProperty, String.valueOf(agents.size()));
		}

		Agent agentForInfo = null;
		if(infoForAgentByIndex >= 0 && infoForAgentByIndex < agents.size()) {
			agentForInfo = agents.get(infoForAgentByIndex);
		}
		if(infoForAgentByName != null) {
			for(Agent agent : agents) {
				if(agent.getName().equalsIgnoreCase(infoForAgentByName))
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
