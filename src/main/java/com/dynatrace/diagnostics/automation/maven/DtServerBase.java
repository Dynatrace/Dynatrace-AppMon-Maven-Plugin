package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.org.apache.http.client.utils.URIBuilder;
import com.dynatrace.sdk.server.BasicServerConfiguration;
import com.dynatrace.sdk.server.DynatraceClient;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.net.URI;
import java.net.URISyntaxException;

public abstract class DtServerBase extends AbstractMojo {

	/** @parameter default-value="${project}" */
	protected org.apache.maven.project.MavenProject mavenProject;

	/**
	 * The username
	 * @parameter expression="${dynaTrace.username}" default-value="admin"
	 */
	private String username = null;

	/**
	 * The password
	 * 
	 * @parameter expression="${dynaTrace.password}" default-value="admin"
	 */
	private String password = null;

	/**
	 * The dynaTrace server URL
	 * 
	 * @parameter expression="${dynaTrace.serverUrl}" default-value="https://localhost:8021"
	 */
	private String serverUrl = null;

	private static final String PROTOCOL_WITHOUT_SSL = "http";
	private static final String PROTOCOL_WITH_SSL = "https";

	private DynatraceClient dynatraceClient;

	/* TODO: default values for BasicServerConfiguration should be better-looking */
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
				throw new URISyntaxException(protocol, "Invalid protocol name in serverUrl"); //maybe something better?
			}

			return new BasicServerConfiguration(this.getUsername(), this.getPassword(), ssl, host, port, false, BasicServerConfiguration.DEFAULT_CONNECTION_TIMEOUT);
		} catch (URISyntaxException e) {
			throw new MojoExecutionException(e.getMessage(), e); //? proper way?
		}
	}

	public DynatraceClient getDynatraceClient() throws MojoExecutionException {
		if (this.dynatraceClient == null) {
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
}
