package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.exceptions.ServerResponseException;
import com.dynatrace.sdk.server.sessions.Sessions;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Parameter;

@Execute(goal = "stopRecording", phase = LifecyclePhase.POST_INTEGRATION_TEST)
public class DtStopRecording extends DtServerProfileBase {

	@Parameter(property = "dynaTrace.sessionNameProperty")
	private String sessionNameProperty;

	@Parameter(property = "dynaTrace.doReanalyzeSession")
	private boolean doReanalyzeSession = false;

	@Parameter(property = "dynaTrace.reanalyzeSessionTimeout", defaultValue = "60000")
	private int reanalyzeSessionTimeout = 60000;

	@Parameter(property = "dynaTrace.reanalyzeSessionPollingInterval", defaultValue = "5000")
	private int reanalyzeSessionPollingInterval = 5000;

	@Parameter(property = "dynaTrace.stopDelay", defaultValue = "0")
	private int stopDelay = 0;

	@Parameter(property = "dynaTrace.reanalyzeStatusProperty")
	private String reanalyzeStatusProperty;
		
	public void execute() throws MojoExecutionException {
		try {
			Thread.sleep(stopDelay);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		try {
			Sessions sessions = new Sessions(this.getDynatraceClient());
			String sessionName = sessions.stopRecording(this.getProfileName());

			System.out.println("Stopped recording on " + getProfileName() + " with SessionName " + sessionName); //$NON-NLS-1$ //$NON-NLS-2$

			if (getSessionNameProperty() != null && getSessionNameProperty().length() > 0) {
				mavenProject.getProperties().setProperty(getSessionNameProperty(), sessionName);
			}

			if (doReanalyzeSession) {
				boolean reanalyzeFinished = sessions.getReanalysisStatus(sessionName);
				if (sessions.reanalyze(sessionName)) {
					int timeout = reanalyzeSessionTimeout;
					while (!reanalyzeFinished && (timeout > 0)) {
						try {
							java.lang.Thread.sleep(getReanalyzeSessionPollingInterval());
							timeout -= getReanalyzeSessionPollingInterval();
						} catch (InterruptedException e) {
						}

						reanalyzeFinished = sessions.getReanalysisStatus(sessionName);
					}
				}

				if (getReanalyzeStatusProperty() != null && getReanalyzeStatusProperty().length() > 0) {
					mavenProject.getProperties().setProperty(getReanalyzeStatusProperty(), String.valueOf(reanalyzeFinished));
				}
			}
		} catch (ServerConnectionException | ServerResponseException e) {
			throw new MojoExecutionException(e.getMessage(), e);
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
