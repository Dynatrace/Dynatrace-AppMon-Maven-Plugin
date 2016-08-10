package com.dynatrace.diagnostics.automation.maven;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * @goal enableProfile
 * @phase pre-integration-test
 */
public class DtEnableProfile extends DtServerProfileBase {

	/**
	 * Enable or disable the profile
	 * @parameter  expression="${dynaTrace.enable}" default-value="true"
	 * @required
	 */
	private boolean enable;
	
	public void execute() throws MojoExecutionException {
		getEndpoint().enableProfile(getProfileName(), isEnable());
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public boolean isEnable() {
		return enable;
	}
}
