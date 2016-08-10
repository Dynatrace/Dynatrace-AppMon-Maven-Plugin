package com.dynatrace.diagnostics.automation.maven;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * @goal restartServer
 * @phase pre-integration-test
 */
public class DtRestartServer extends DtServerBase {
	
	/**
	 * @property expression="${dynaTrace.restart}" default-value="true"
	 */
	private boolean restart = true;
	
	public void execute() throws MojoExecutionException {
		if(restart)
			getEndpoint().restartServer();
		else
			getEndpoint().shutdownServer();
	}

	public void setRestart(boolean restart) {
		this.restart = restart;
	}
}
