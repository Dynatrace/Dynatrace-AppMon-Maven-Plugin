package com.dynatrace.diagnostics.automation.maven;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * @goal threadDump
 * @phase verify
 */
public class DtThreadDump extends DtAgentBase {

	/**
	 * @parameter expression="${dynaTrace.sessionLocked}"
	 */
	private boolean sessionLocked;
	
	/**
	 * @parameter expression="${dynaTrace.threadDumpNameProperty}"
	 */
	private String threadDumpNameProperty;
	
	/**
	 * @parameter expression="${dynaTrace.waitForDumpTimeout}" default-value="60000"
	 */
	private int waitForDumpTimeout = 60000;
	
	/**
	 * @parameter expression="${dynaTrace.waitForDumpPollingInterval}" default-value="5000"
	 */	
	private int waitForDumpPollingInterval = 5000;
	
	/**
	 * @parameter expression="${dynaTrace.dumpStatusProperty}"
	 */	
	private String dumpStatusProperty;	
	
	@Override
	public void execute() throws MojoExecutionException {
		System.out.println("Creating Thread Dump for " + getProfileName() + "-" + getAgentName() + "-" + getHostName() + "-" + getProcessId()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		
		String threadDump = getEndpoint().threadDump(getProfileName(), getAgentName(), getHostName(), getProcessId(), isSessionLocked());
		if(threadDumpNameProperty != null && threadDumpNameProperty.length() > 0) {
			mavenProject.getProperties().setProperty(threadDumpNameProperty, threadDump);
		}
		
		int timeout = waitForDumpTimeout;
		boolean dumpFinished = getEndpoint().threadDumpStatus(getProfileName(), threadDump).isResultValueTrue();
		while(!dumpFinished && (timeout > 0)) {
			try {
				java.lang.Thread.sleep(waitForDumpPollingInterval);
				timeout -= waitForDumpPollingInterval;
			} catch (InterruptedException e) {
			}
			
			dumpFinished = getEndpoint().threadDumpStatus(getProfileName(), threadDump).isResultValueTrue();
		}
		
		if(dumpStatusProperty != null && dumpStatusProperty.length() > 0) {
			mavenProject.getProperties().setProperty(dumpStatusProperty, String.valueOf(dumpFinished));
		}
	}

	public void setSessionLocked(boolean sessionLocked) {
		this.sessionLocked = sessionLocked;
	}

	public boolean isSessionLocked() {
		return sessionLocked;
	}

	public void setThreadDumpNameProperty(String threadDumpNameProperty) {
		this.threadDumpNameProperty = threadDumpNameProperty;
	}

	public String getThreadDumpNameProperty() {
		return threadDumpNameProperty;
	}

	public void setWaitForDumpTimeout(int waitForDumpTimeout) {
		this.waitForDumpTimeout = waitForDumpTimeout;
	}

	public int getWaitForDumpTimeout() {
		return waitForDumpTimeout;
	}

	public void setWaitForDumpPollingInterval(int waitForDumpPollingInterval) {
		this.waitForDumpPollingInterval = waitForDumpPollingInterval;
	}

	public int getWaitForDumpPollingInterval() {
		return waitForDumpPollingInterval;
	}

	public void setDumpStatusProperty(String dumpStatusProperty) {
		this.dumpStatusProperty = dumpStatusProperty;
	}

	public String getDumpStatusProperty() {
		return dumpStatusProperty;
	}
}
