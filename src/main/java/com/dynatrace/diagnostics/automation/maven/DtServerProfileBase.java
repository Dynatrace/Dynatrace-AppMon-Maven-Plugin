package com.dynatrace.diagnostics.automation.maven;

import org.apache.maven.plugins.annotations.Parameter;

abstract class DtServerProfileBase extends DtServerBase {

	/**
	 * The system profile to use.
	 */
	@Parameter(property = "dynaTrace.systemProfile", required = true)
	private String profileName;

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public String getProfileName() {
		return profileName;
	}
	
}
