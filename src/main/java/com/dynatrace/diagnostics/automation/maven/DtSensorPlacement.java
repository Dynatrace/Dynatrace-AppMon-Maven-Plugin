package com.dynatrace.diagnostics.automation.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.dynatrace.diagnostics.automation.rest.sdk.RESTEndpoint;


public class DtSensorPlacement extends DtServerBase{
	/**
	 * @parameter expression="${dynaTrace.agentId}"
	 */
	private int agentId;

	public void execute() throws MojoExecutionException, MojoFailureException {
		RESTEndpoint endpoint=getEndpoint();
		endpoint.hotSensorPlacement(agentId);
	}

	public int getAgentId() {
		return agentId;
	}

	public void setAgentId(int agentId) {
		this.agentId = agentId;
	}
}
