package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.exceptions.ServerResponseException;
import com.dynatrace.sdk.server.systemprofiles.SystemProfiles;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "activateConfiguration", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class DtActivateConfiguration extends DtServerProfileBase {

	/**
	 * The configuration name to activate
	 */
	@Parameter(property = "dynaTrace.configuration", required = true)
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
