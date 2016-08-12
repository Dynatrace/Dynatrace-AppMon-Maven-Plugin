package com.dynatrace.diagnostics.automation.maven;

import org.apache.maven.plugins.annotations.Parameter;

public abstract class DtAgentBase extends DtServerProfileBase {

	/**
	 * The host name of the agent
	 */
	@Parameter(property = "dynaTrace.agentHostName", required = true)
	private String hostName;
	
	/**
	 * The agents name
	 */
	@Parameter(property = "dynaTrace.agentName", required = true)
	private String agentName;
	
	/**
	 * The process id of the agent
	 */
	@Parameter(property = "dynaTrace.agentProcessId", required = true)
	private int processId;
	
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public String getHostName() {
		return hostName;
	}
	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}
	public String getAgentName() {
		return agentName;
	}
	public void setProcessId(int processId) {
		this.processId = processId;
	}
	public int getProcessId() {
		return processId;
	}
}
