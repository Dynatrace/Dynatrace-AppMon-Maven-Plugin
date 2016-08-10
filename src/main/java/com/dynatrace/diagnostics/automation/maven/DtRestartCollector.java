package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.agentsandcollectors.AgentsAndCollectors;
import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.exceptions.ServerResponseException;
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
		AgentsAndCollectors agentsAndCollectors = new AgentsAndCollectors(this.getDynatraceClient());

		try {
			if (this.restart) {
				agentsAndCollectors.restartCollector(this.getCollector());
			} else {
				agentsAndCollectors.shutdownCollector(this.getCollector());
			}
		} catch (ServerConnectionException | ServerResponseException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
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
