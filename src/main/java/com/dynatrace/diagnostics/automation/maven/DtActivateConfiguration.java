package com.dynatrace.diagnostics.automation.maven;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * @goal activateConfiguration
 * @phase pre-integration-test
 */
public class DtActivateConfiguration extends DtServerProfileBase {

	/**
	 * The configuration name to activate
	 * @parameter expression="${dynaTrace.configuration}"
	 * @required
	 */
	private String configuration;
	
	public void execute() throws MojoExecutionException {
		getEndpoint().activateConfiguration(getProfileName(), getConfiguration());
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	public String getConfiguration() {
		return configuration;
	}
}
