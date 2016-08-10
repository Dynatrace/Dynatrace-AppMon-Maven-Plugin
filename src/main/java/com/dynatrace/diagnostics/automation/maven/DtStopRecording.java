package com.dynatrace.diagnostics.automation.maven;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * @goal stopRecording
 * @phase post-integration-test
 */
public class DtStopRecording extends DtServerProfileBase {

	/**
	 * @parameter expression="${dynaTrace.sessionNameProperty}"
	 */
	private String sessionNameProperty;
	
	/**
	 * @parameter expression="${dynaTrace.doReanalyzeSession}"
	 */
	private boolean doReanalyzeSession = false;
	
	/**
	 * @parameter expression="${dynaTrace.reanalyzeSessionTimeout}" default-value="60000"
	 */
	private int reanalyzeSessionTimeout = 60000;
	
	/**
	 * @parameter expression="${dynaTrace.reanalyzeSessionPollingInterval}" default-value="5000"
	 */	
	private int reanalyzeSessionPollingInterval = 5000;

	/**
	 * @parameter expression="${dynaTrace.stopDelay}" default-value="0"
	 */
	private int stopDelay = 0;

	/**
	 * @parameter expression="${dynaTrace.reanalyzeStatusProperty}"
	 */	
	private String reanalyzeStatusProperty;
		
	public void execute() throws MojoExecutionException {
		try {
			Thread.sleep(stopDelay);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		String sessionName = getEndpoint().stopRecording(getProfileName());
		
		System.out.println("Stopped recording on " + getProfileName() + " with SessionName " + sessionName); //$NON-NLS-1$ //$NON-NLS-2$
				
		if(getSessionNameProperty() != null && getSessionNameProperty().length() > 0) {
			mavenProject.getProperties().setProperty(getSessionNameProperty(), sessionName);
		}
		
		if(doReanalyzeSession) {
			boolean reanalyzeFinished = getEndpoint().reanalyzeSessionStatus(sessionName);
			if(getEndpoint().reanalyzeSession(sessionName)) {			
				int timeout = reanalyzeSessionTimeout;
				while(!reanalyzeFinished && (timeout > 0)) {
					try {
						java.lang.Thread.sleep(getReanalyzeSessionPollingInterval());
						timeout -= getReanalyzeSessionPollingInterval();
					} catch (InterruptedException e) {
					}
					
					reanalyzeFinished = getEndpoint().reanalyzeSessionStatus(sessionName);
				}
			}
			
			if(getReanalyzeStatusProperty() != null && getReanalyzeStatusProperty().length() > 0) {
				mavenProject.getProperties().setProperty(getReanalyzeStatusProperty(), String.valueOf(reanalyzeFinished));
			}
		}
	}

	public void setSessionNameProperty(String sessionNameProperty) {
		this.sessionNameProperty = sessionNameProperty;
	}

	public String getSessionNameProperty() {
		if(sessionNameProperty == null) {
			if(sessionNameProperty == null) {
				String dtSessionNameProperty = mavenProject.getProperties().getProperty("dtSessionNameProperty"); //$NON-NLS-1$
				if(dtSessionNameProperty != null && dtSessionNameProperty.length() > 0)
					sessionNameProperty = dtSessionNameProperty;
			}			
		}		
		return sessionNameProperty;
	}

	public void setDoReanalyzeSession(boolean doReanalyzeSession) {
		this.doReanalyzeSession = doReanalyzeSession;
	}

	public boolean isDoReanalyzeSession() {
		return doReanalyzeSession;
	}

	public void setReanalyzeSessionTimeout(int reanalyzeSessionTimeout) {
		this.reanalyzeSessionTimeout = reanalyzeSessionTimeout;
	}

	public int getReanalyzeSessionTimeout() {
		return reanalyzeSessionTimeout;
	}

	public void setReanalyzeStatusProperty(String reanalyzeStatusProperty) {
		this.reanalyzeStatusProperty = reanalyzeStatusProperty;
	}

	public String getReanalyzeStatusProperty() {
		return reanalyzeStatusProperty;
	}

	public void setReanalyzeSessionPollingInterval(
			int reanalyzeSessionPollingInterval) {
		this.reanalyzeSessionPollingInterval = reanalyzeSessionPollingInterval;
	}

	public int getReanalyzeSessionPollingInterval() {
		return reanalyzeSessionPollingInterval;
	}

	public int getStopDelay() {
		return stopDelay;
	}

	public void setStopDelay(int stopDelay) {
		this.stopDelay = stopDelay;
	}
}
