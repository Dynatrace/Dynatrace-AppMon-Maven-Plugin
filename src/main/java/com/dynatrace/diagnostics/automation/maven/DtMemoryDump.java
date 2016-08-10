package com.dynatrace.diagnostics.automation.maven;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * @goal memoryDump
 * @phase verify
 */
public class DtMemoryDump extends DtAgentBase {

	/**
	 * @parameter expression="${dynaTrace.dumpType}" default-value="simple"
	 */
	private String dumpType;

	/**
	 * @parameter expression="${dynaTrace.sessionLocked}" default-value="true"
	 */
	private boolean sessionLocked;
	
	/**
	 * @parameter expression="${dynaTrace.memoryDumpNameProperty}"
	 */	
	private String memoryDumpNameProperty;
	
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

	/**
	 * @parameter expression="${dynaTrace.doGc}" default-value="false"
	 */
	private boolean doGc;

	/**
	 * @parameter expression="${dynaTrace.autoPostProcess}" default-value="false"
	 */
	private boolean autoPostProcess;

	/**
	 * @parameter expression="${dynaTrace.capturePrimitives}" default-value="false"
	 */
	private boolean capturePrimitives;

	/**
	 * @parameter expression="${dynaTrace.captureStrings}" default-value="false"
	 */
	private boolean captureStrings;

	@Override
	public void execute() throws MojoExecutionException {
		System.out.println("Creating Memory Dump for " + getProfileName() + "-" + getAgentName() + "-" + getHostName() + "-" + getProcessId()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		String memoryDump = getEndpoint().memoryDump(getProfileName(), getAgentName(), getHostName(), getProcessId(), getDumpType(), isSessionLocked(), getCaptureStrings(), getCapturePrimitives(), getAutoPostProcess(), getDoGc());
		if(memoryDumpNameProperty != null && memoryDumpNameProperty.length() > 0) {
			mavenProject.getProperties().setProperty(memoryDumpNameProperty, memoryDump);
		}

		if(memoryDump == null || memoryDump.length() == 0) {
			throw new MojoExecutionException("Memory Dump wasnt taken"); //$NON-NLS-1$
		}
		
		int timeout = waitForDumpTimeout;
		boolean dumpFinished = getEndpoint().memoryDumpStatus(getProfileName(), memoryDump).isResultValueTrue();
		while(!dumpFinished && (timeout > 0)) {
			try {
				java.lang.Thread.sleep(waitForDumpPollingInterval);
				timeout -= waitForDumpPollingInterval;
			} catch (InterruptedException e) {
			}
			
			dumpFinished = getEndpoint().memoryDumpStatus(getProfileName(), memoryDump).isResultValueTrue();
		}
		
		if(dumpStatusProperty != null && dumpStatusProperty.length() > 0) {
			mavenProject.getProperties().setProperty(dumpStatusProperty, String.valueOf(dumpFinished));
		}
	}

	private boolean getCaptureStrings() {
		return captureStrings;
	}

	private boolean getCapturePrimitives() {
		return capturePrimitives;
	}

	private boolean getAutoPostProcess() {
		return autoPostProcess;
	}

	private boolean getDoGc() {
		return doGc;
	}

	public void setDoGc(boolean doGc) {
		this.doGc = doGc;
	}

	public void setAutoPostProcess(boolean autoPostProcess) {
		this.autoPostProcess = autoPostProcess;
	}

	public void setCapturePrimitives(boolean capturePrimitives) {
		this.capturePrimitives = capturePrimitives;
	}

	public void setCaptureStrings(boolean captureStrings) {
		this.captureStrings = captureStrings;
	}

	public void setDumpType(String dumpType) {
		this.dumpType = dumpType;
	}

	public String getDumpType() {
		return dumpType;
	}

	public void setSessionLocked(boolean sessionLocked) {
		this.sessionLocked = sessionLocked;
	}

	public boolean isSessionLocked() {
		return sessionLocked;
	}

	public void setMemoryDumpNameProperty(String memoryDumpNameProperty) {
		this.memoryDumpNameProperty = memoryDumpNameProperty;
	}

	public String getMemoryDumpNameProperty() {
		return memoryDumpNameProperty;
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
