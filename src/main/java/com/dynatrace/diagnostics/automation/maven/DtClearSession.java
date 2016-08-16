package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.exceptions.ServerResponseException;
import com.dynatrace.sdk.server.sessions.Sessions;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "clearSession", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class DtClearSession extends DtServerProfileBase {

	public void execute() throws MojoExecutionException {
		Sessions sessions = new Sessions(this.getDynatraceClient());

		try {
			sessions.clear(this.getProfileName());
		} catch (ServerResponseException | ServerConnectionException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}	
}
