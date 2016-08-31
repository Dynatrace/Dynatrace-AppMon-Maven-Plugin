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

import com.dynatrace.sdk.server.BasicServerConfiguration;
import com.dynatrace.sdk.server.DynatraceClient;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DtStartTestServerConfigurationMojoTest extends AbstractMojoTestCase {

    @Before
    @Test
    public void testBuildServerConfigurationHttpWithSuccess() {
        DtStartTest startTest = new DtStartTest();

        startTest.setUsername("admin");
        startTest.setPassword("adminPassword");
        startTest.setIgnoreSSLErrors(true);
        startTest.setServerUrl("http://localhost:8080");

        DynatraceClient client;

        try {
            startTest.setDynatraceClientWithCustomHttpClient(null);
            client = startTest.getDynatraceClient();

            assertTrue(client.getConfiguration() instanceof BasicServerConfiguration);
            BasicServerConfiguration configuration = (BasicServerConfiguration) client.getConfiguration();

            assertThat(configuration.getName(), is("admin"));
            assertThat(configuration.getPassword(), is("adminPassword"));
            assertThat(configuration.isValidateCertificates(), is(false));
            assertThat(configuration.getHost(), is("localhost"));
            assertThat(configuration.getPort(), is(8080));
            assertThat(configuration.isSSL(), is(false));

            assertThat(startTest.getIgnoreSSLErrors(), is(true));

        } catch (MojoExecutionException e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testBuildServerConfigurationHttpsWithSuccess() {
        DtStartTest startTest = new DtStartTest();

        startTest.setUsername("admin");
        startTest.setPassword("adminPassword");
        startTest.setIgnoreSSLErrors(true);
        startTest.setServerUrl("https://localhost:8080");

        DynatraceClient client;

        try {
            startTest.setDynatraceClientWithCustomHttpClient(null);
            client = startTest.getDynatraceClient();

            assertTrue(client.getConfiguration() instanceof BasicServerConfiguration);
            BasicServerConfiguration configuration = (BasicServerConfiguration) client.getConfiguration();

            assertThat(configuration.getName(), is("admin"));
            assertThat(configuration.getPassword(), is("adminPassword"));
            assertThat(configuration.isValidateCertificates(), is(false));
            assertThat(configuration.getHost(), is("localhost"));
            assertThat(configuration.getPort(), is(8080));
            assertThat(configuration.isSSL(), is(true));

        } catch (MojoExecutionException e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testBuildServerConfigurationWithFail() {
        DtStartTest startTest = new DtStartTest();

        startTest.setUsername("admin");
        startTest.setPassword("adminPassword");
        startTest.setIgnoreSSLErrors(true);
        startTest.setServerUrl("ftp://localhost:8080");


        try {
            startTest.setDynatraceClientWithCustomHttpClient(null);

            fail("Exception is expected to be thrown.");
        } catch (MojoExecutionException e) {
            assertThat(e.getMessage(), StringContains.containsString("ftp"));
        }
    }
}