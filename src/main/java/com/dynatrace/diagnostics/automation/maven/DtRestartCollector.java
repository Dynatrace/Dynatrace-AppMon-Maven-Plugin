package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.agentsandcollectors.AgentsAndCollectors;
import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.exceptions.ServerResponseException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "restartCollector")
@Execute(phase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class DtRestartCollector extends DtServerBase {

	@Parameter(property = "dynaTrace.restart", defaultValue = "true")
	private boolean restart = true;

	@Parameter(property = "dynaTrace.collector", required = true)
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
