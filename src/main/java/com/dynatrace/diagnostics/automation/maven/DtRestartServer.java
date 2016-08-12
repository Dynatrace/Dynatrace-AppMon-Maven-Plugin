package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.exceptions.ServerResponseException;
import com.dynatrace.sdk.server.servermanagement.ServerManagement;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Parameter;

@Execute(goal = "restartServer", phase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class DtRestartServer extends DtServerBase {

	@Parameter(property = "dynaTrace.restart", defaultValue = "true")
	private boolean restart = true;
	
	public void execute() throws MojoExecutionException {
		ServerManagement serverManagement = new ServerManagement(this.getDynatraceClient());

		try {
			if (this.restart) {
				serverManagement.restart();
			} else {
				serverManagement.shutdown();
			}
		} catch (ServerConnectionException | ServerResponseException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	public void setRestart(boolean restart) {
		this.restart = restart;
	}
}
