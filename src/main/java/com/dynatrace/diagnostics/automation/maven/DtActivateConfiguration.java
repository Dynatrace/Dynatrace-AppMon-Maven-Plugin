package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.exceptions.ServerResponseException;
import com.dynatrace.sdk.server.systemprofiles.SystemProfiles;
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
		try {
			SystemProfiles systemProfiles = new SystemProfiles(this.getDynatraceClient());
			systemProfiles.activateProfileConfiguration(this.getProfileName(), this.getConfiguration());
		} catch (ServerConnectionException | ServerResponseException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	public String getConfiguration() {
		return configuration;
	}
}
