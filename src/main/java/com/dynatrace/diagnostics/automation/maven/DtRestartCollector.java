package com.dynatrace.diagnostics.automation.maven;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * @goal restartCollector
 * @phase pre-integration-test
 */
public class DtRestartCollector extends DtServerBase {
	
	/**
	 * @property expression="${dynaTrace.restart}" default-value="true"
	 */
	private boolean restart = true;
	
	/**
	 * @property expression="${dynaTrace.collector}"
	 * @required
	 */	
	private String collector;
	
	public void execute() throws MojoExecutionException {
		if(restart)
			getEndpoint().restartCollector(getCollector());
		else
			getEndpoint().shutdownCollector(getCollector());
	}

	public void setRestart(boolean restart) {
		this.restart = restart;
	}

	public void setCollector(String collector) {
		this.collector = collector;
	}

	public String getCollector() {
		return collector;
	}
}
