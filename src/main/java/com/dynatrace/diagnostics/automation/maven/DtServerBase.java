package com.dynatrace.diagnostics.automation.maven;

import org.apache.maven.plugin.AbstractMojo;

import com.dynatrace.diagnostics.automation.rest.sdk.RESTEndpoint;

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

	private RESTEndpoint endpoint = null;
	public RESTEndpoint getEndpoint() {
		if(endpoint == null) {
			getLog().info("Connection to dynaTrace Server via " + getServerUrl() + " with username " + getUsername()); //$NON-NLS-1$ //$NON-NLS-2$
			endpoint = new RESTEndpoint(getUsername(), getPassword(), getServerUrl());
		}
		return endpoint;
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
