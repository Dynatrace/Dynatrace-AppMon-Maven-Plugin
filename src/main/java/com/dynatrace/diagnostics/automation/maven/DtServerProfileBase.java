package com.dynatrace.diagnostics.automation.maven;

public abstract class DtServerProfileBase extends DtServerBase {

	/**
	 * The system profile to use.
	 * 
	 * @parameter expression="${dynaTrace.systemProfile}"
	 * @required
	 */
	private String profileName;

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public String getProfileName() {
		return profileName;
	}
	
}
