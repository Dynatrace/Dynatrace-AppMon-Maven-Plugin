package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.exceptions.ServerResponseException;
import com.dynatrace.sdk.server.sessions.Sessions;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * @goal reanalyzeSession
 * @phase verify
 */
public class DtReanalyzeSession extends DtServerBase {

	/**
	 * @property expression="${dynaTrace.sessionName}"
	 * @required
	 */
	private String sessionName;
	
	/**
	 * @property expression="${dynaTrace.reanalyzeSessionTimeout}" default-value="60000"
	 */
	private int reanalyzeSessionTimeout = 60000;

	/**
	 * @property expression="${dynaTrace.reanalyzeSessionPollingInterval}" default-value="5000"
	 */
	private int reanalyzeSessionPollingInterval = 5000;

	/**
	 * @property expression="${dynaTrace.reanalyzeStatusProperty}"
	 */
	private String reanalyzeStatusProperty;

	public void execute() throws MojoExecutionException {
		boolean reanalyzeFinished = false;

		Sessions sessions = new Sessions(this.getDynatraceClient());

		try {
			if (sessions.reanalyze(this.getSessionName())) {
				int timeout = reanalyzeSessionTimeout;
				reanalyzeFinished = sessions.getReanalysisStatus(this.getSessionName());
				while (!reanalyzeFinished && (timeout > 0)) {
					try {
						java.lang.Thread.sleep(getReanalyzeSessionPollingInterval());
						timeout -= getReanalyzeSessionPollingInterval();
					} catch (InterruptedException e) {
					}

					reanalyzeFinished = sessions.getReanalysisStatus(this.getSessionName());
				}
			}
		} catch (ServerConnectionException | ServerResponseException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}

		if (getReanalyzeStatusProperty() != null && getReanalyzeStatusProperty().length() > 0) {
			mavenProject.getProperties().setProperty(getReanalyzeStatusProperty(), String.valueOf(reanalyzeFinished));
		}
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

	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}

	public String getSessionName() {
		return sessionName;
	}

	public void setReanalyzeSessionPollingInterval(
			int reanalyzeSessionPollingInterval) {
		this.reanalyzeSessionPollingInterval = reanalyzeSessionPollingInterval;
	}

	public int getReanalyzeSessionPollingInterval() {
		return reanalyzeSessionPollingInterval;
	}	
}
