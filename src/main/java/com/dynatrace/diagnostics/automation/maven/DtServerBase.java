package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.org.apache.http.client.utils.URIBuilder;
import com.dynatrace.sdk.server.BasicServerConfiguration;
import com.dynatrace.sdk.server.DynatraceClient;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.net.URI;
import java.net.URISyntaxException;

public abstract class DtServerBase extends AbstractMojo {
	private static final String PROTOCOL_WITHOUT_SSL = "http";
	private static final String PROTOCOL_WITH_SSL = "https";

	/** Use unlimited connection timeout */
	private static final int CONNECTION_TIMEOUT = 0;

	/** Maven project that contains runtime properties */
	@Parameter(defaultValue = "${project}")
	protected MavenProject mavenProject;

	/**  The username */
	@Parameter(property = "dynaTrace.username", defaultValue = "admin")
	private String username = null;

	/** The password */
	@Parameter(property = "dynaTrace.password", defaultValue = "admin")
	private String password = null;

	/** The dynaTrace server URL */
	@Parameter(property = "dynaTrace.serverUrl", defaultValue = "https://localhost:8021")
	private String serverUrl = null;

	/** Ignore SSL errors */
	@Parameter(property = "dynaTrace.ignoreSSLErrors", defaultValue = "true")
	private boolean ignoreSSLErrors = true;

	private DynatraceClient dynatraceClient;

	private BasicServerConfiguration buildServerConfiguration() throws MojoExecutionException {
		try {
			URIBuilder uriBuilder = new URIBuilder(this.getServerUrl());
			URI uri = uriBuilder.build();

			String protocol = uri.getScheme();
			String host = uri.getHost();
			int port = uri.getPort();
			boolean ssl = BasicServerConfiguration.DEFAULT_SSL;

			if (protocol != null && (protocol.equals(PROTOCOL_WITH_SSL) || protocol.equals(PROTOCOL_WITHOUT_SSL))) {
				ssl = protocol.equals(PROTOCOL_WITH_SSL);
			} else {
				throw new URISyntaxException(protocol, "Invalid protocol name in serverUrl");
			}

			return new BasicServerConfiguration(this.getUsername(), this.getPassword(), ssl, host, port, !this.ignoreSSLErrors, CONNECTION_TIMEOUT);
		} catch (URISyntaxException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	public DynatraceClient getDynatraceClient() throws MojoExecutionException {
		if (this.dynatraceClient == null) {
			getLog().info("Connection to dynaTrace Server via " + getServerUrl() + " with username " + getUsername() + ", ignoring SSL errors: " + this.ignoreSSLErrors); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			this.dynatraceClient = new DynatraceClient(this.buildServerConfiguration());
		}

		return this.dynatraceClient;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	public String getUsername() {
		return username;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getPassword() {
		return password;
	}
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}
	public String getServerUrl() {
		return serverUrl;
	}
	public void setIgnoreSSLErrors(boolean ignoreSslErrors) { this.ignoreSSLErrors = ignoreSslErrors; }
	public boolean getIgnoreSSLErrors() { return this.ignoreSSLErrors; }
}
