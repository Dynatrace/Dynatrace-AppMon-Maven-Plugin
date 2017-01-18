/*
 * Dynatrace Maven Plugin
 * Copyright (c) 2008-2016, DYNATRACE LLC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *  Neither the name of the dynaTrace software nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package com.dynatrace.diagnostics.automation.maven;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.dynatrace.diagnostics.automation.util.DtUtil;
import com.dynatrace.sdk.server.BasicServerConfiguration;
import com.dynatrace.sdk.server.DynatraceClient;

/**
 * Defines base class for maven goals which are using server properties
 */
abstract class DtServerBase extends AbstractMojo {
    private static final String PROTOCOL_WITHOUT_SSL = "http";
    private static final String PROTOCOL_WITH_SSL = "https";

    /**
     * Use unlimited connection timeout
     */
    private static final int CONNECTION_TIMEOUT = 0;

    /**
     * Maven project that contains runtime properties
     */
    @Parameter(defaultValue = "${project}")
    private MavenProject mavenProject;

    /* Properties with default values available in Maven Project environment */
    @Parameter(property = "dynaTrace.username", defaultValue = "admin")
    private String username;

    @Parameter(property = "dynaTrace.password", defaultValue = "admin")
    private String password;

    @Parameter(property = "dynaTrace.serverUrl", defaultValue = "https://localhost:8021")
    private String serverUrl;

    @Parameter(property = "dynaTrace.ignoreSSLErrors", defaultValue = "true")
    private boolean ignoreSSLErrors;

    /**
     * contains Dynatrace client
     */
    private DynatraceClient dynatraceClient;

    /**
     * Builds configuration required for {@link DynatraceClient}
     *
     * @return {@link BasicServerConfiguration} containing configuration based on parameters provided in properties
     * @throws MojoExecutionException whenever connecting to the server, parsing a response or execution fails
     */
    private BasicServerConfiguration buildServerConfiguration() throws MojoExecutionException {
        try {
            URIBuilder uriBuilder = new URIBuilder(this.serverUrl);
            URI uri = uriBuilder.build();

            String protocol = uri.getScheme();
            String host = uri.getHost();
            int port = uri.getPort();
            boolean ssl = this.isProtocolCompatibleWithSsl(protocol);

            return new BasicServerConfiguration(this.username, this.password, ssl, host, port, !this.ignoreSSLErrors, CONNECTION_TIMEOUT);
        } catch (URISyntaxException | IllegalArgumentException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    /**
     * Checks whether given protocol is http (without SSL) or https (with SSL)
     *
     * @param protocol - protocol name extracted from url
     * @return boolean that describes that the given protocol has SSL
     * @throws IllegalArgumentException whenever given protocol name isn't valid (isn't http or https)
     */
    private boolean isProtocolCompatibleWithSsl(String protocol) throws IllegalArgumentException {
        if (!DtUtil.isEmpty(protocol) && (protocol.equals(PROTOCOL_WITH_SSL) || protocol.equals(PROTOCOL_WITHOUT_SSL))) {
            return protocol.equals(PROTOCOL_WITH_SSL);
        }

        throw new IllegalArgumentException(String.format("Invalid protocol name: %s", protocol), new Exception());
    }

    /**
     * Returns {@link DynatraceClient} required for Server SDK classes
     *
     * @return {@link DynatraceClient} with parameters provided in properties
     * @throws MojoExecutionException whenever execution fails
     */
    public DynatraceClient getDynatraceClient() throws MojoExecutionException {
        if (this.dynatraceClient == null) {
            this.getLog().info(String.format("Connection to dynaTrace Server via %s with username %s, ignoring SSL errors: %b", this.serverUrl, this.username, this.ignoreSSLErrors));
            this.dynatraceClient = new DynatraceClient(this.buildServerConfiguration());
        }

        return this.dynatraceClient;
    }

    /**
     * Returns {@link DynatraceClient} required for Server SDK classes
     * <p>
     * Used only for testing purposes
     *
     * @param client - user-defined {@link CloseableHttpClient}
     * @return {@link DynatraceClient} with parameters provided in properties
     * @throws MojoExecutionException whenever execution fails
     */
    public void setDynatraceClientWithCustomHttpClient(CloseableHttpClient client) throws MojoExecutionException {
        this.getLog().info(String.format("Connection to dynaTrace Server via %s with username %s, ignoring SSL errors: %b", this.serverUrl, this.username, this.ignoreSSLErrors));
        this.dynatraceClient = new DynatraceClient(this.buildServerConfiguration(), client);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public boolean getIgnoreSSLErrors() {
        return this.ignoreSSLErrors;
    }

    public void setIgnoreSSLErrors(boolean ignoreSslErrors) {
        this.ignoreSSLErrors = ignoreSslErrors;
    }

    public MavenProject getMavenProject() {
        return mavenProject;
    }

    public void setMavenProject(MavenProject mavenProject) {
        this.mavenProject = mavenProject;
    }
}
