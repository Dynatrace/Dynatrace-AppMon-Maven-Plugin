package com.dynatrace.diagnostics.automation.maven;

public abstract class DtAgentBase extends DtServerProfileBase {

	/**
	 * The host name of the agent
	 * @parameter expression="${dynaTrace.agentHostName}"
	 * @required
	 */
	private String hostName;
	
	/**
	 * The agents name
	 * @parameter expression="${dynaTrace.agentName}"
	 * @required
	 */
	private String agentName;
	
	/**
	 * The process id of the agent
	 * @parameter expression="${dynaTrace.agentProcessId}"
	 * @required
	 */	
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
