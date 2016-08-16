package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.exceptions.ServerResponseException;
import com.dynatrace.sdk.server.sessions.Sessions;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "reanalyzeSession", defaultPhase = LifecyclePhase.VERIFY)
public class DtReanalyzeSession extends DtServerBase {

	@Parameter(property = "dynaTrace.sessionName", required = true)
	private String sessionName;

	@Parameter(property = "dynaTrace.reanalyzeSessionTimeout")
	private int reanalyzeSessionTimeout = 60000;

	@Parameter(property = "dynaTrace.reanalyzeSessionPollingInterval")
	private int reanalyzeSessionPollingInterval = 5000;

	@Parameter(property = "dynaTrace.reanalyzeStatusProperty")
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
