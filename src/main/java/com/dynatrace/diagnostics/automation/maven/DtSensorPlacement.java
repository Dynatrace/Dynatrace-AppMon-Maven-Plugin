package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.agentsandcollectors.AgentsAndCollectors;
import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.exceptions.ServerResponseException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.dynatrace.diagnostics.automation.rest.sdk.RESTEndpoint;


public class DtSensorPlacement extends DtServerBase{
	/**
	 * @parameter expression="${dynaTrace.agentId}"
	 */
	private int agentId;

	public void execute() throws MojoExecutionException, MojoFailureException {
		AgentsAndCollectors agentsAndCollectors = new AgentsAndCollectors(this.getDynatraceClient());

		try {
			agentsAndCollectors.placeHotSensor(agentId);
		} catch (ServerConnectionException | ServerResponseException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	public int getAgentId() {
		return agentId;
	}

	public void setAgentId(int agentId) {
		this.agentId = agentId;
	}
}
