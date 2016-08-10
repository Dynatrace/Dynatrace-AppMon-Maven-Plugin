package com.dynatrace.diagnostics.automation.maven;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * @goal clearSession
 * @phase pre-integration-test
 */
public class DtClearSession extends DtServerProfileBase {

	public void execute() throws MojoExecutionException {
		getEndpoint().clearSession(getProfileName());
	}	
}
