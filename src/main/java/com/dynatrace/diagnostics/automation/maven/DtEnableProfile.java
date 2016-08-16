package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.exceptions.ServerResponseException;
import com.dynatrace.sdk.server.systemprofiles.SystemProfiles;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "enableProfile", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class DtEnableProfile extends DtServerProfileBase {

	/**
	 * Enable or disable the profile
	 */
	@Parameter(property = "dynaTrace.enable", defaultValue = "true", required = true)
	private boolean enable;
	
	public void execute() throws MojoExecutionException {
		try {
			SystemProfiles systemProfiles = new SystemProfiles(this.getDynatraceClient());
			if (this.enable) {
				systemProfiles.enableProfile(this.getProfileName());
			} else {
				systemProfiles.disableProfile(this.getProfileName());
			}
		} catch (ServerConnectionException | ServerResponseException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public boolean isEnable() {
		return enable;
	}
}
