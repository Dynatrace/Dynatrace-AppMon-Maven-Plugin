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

import com.dynatrace.sdk.server.testautomation.TestAutomation;
import com.dynatrace.sdk.server.testautomation.models.CreateTestRunRequest;
import com.dynatrace.sdk.server.testautomation.models.TestCategory;
import com.dynatrace.sdk.server.testautomation.models.TestRun;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Date;
import java.util.Properties;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TestAutomation.class, DtStartTest.class})
public class DtStartTestMojoTest extends AbstractDynatraceMojoTest<DtStartTest> {
    private static final String START_TEST_GOAL_NAME = "startTest";
    private static final String EXAMPLE_TEST_RUN_ID = "7f98a064-d00d-4224-8803-2f87f4988584";

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.applyFreshMojo();

        TestRun testRun = new TestRun(new Date(0L), null, null, TestCategory.UNIT, EXAMPLE_TEST_RUN_ID, null, null, null, null, null, null, null, null, null, null, null, null, null);
        TestAutomation testAutomation = spy(new TestAutomation(this.getMojo().getDynatraceClient()));

        /** define responses */
        doReturn(testRun).when(testAutomation).createTestRun(Mockito.any(CreateTestRunRequest.class));

        whenNew(TestAutomation.class).withAnyArguments().thenReturn(testAutomation);

        /** verify default values */
        assertThat(this.getMojo().isIgnoreVersionTag(), is(false));
    }

    @Override
    protected String getMojoGoalName() {
        return START_TEST_GOAL_NAME;
    }

    @Test
    public void testStartTestWithIgnoreVersionTag() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setMavenProject(new MavenProject());
            this.getMojo().setIgnoreVersionTag(true);
            this.getMojo().setVersionBuild("1");
            this.getMojo().setCategory("unit");

            Properties additionalProperties = new Properties();
            additionalProperties.setProperty("firstProperty", "firstValue");
            additionalProperties.setProperty("secondProperty", "secondValue");
            this.getMojo().setAdditionalProperties(additionalProperties);

            this.getMojo().execute();

            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("dtTestrunID"), is(EXAMPLE_TEST_RUN_ID));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testStartTestWithoutIgnoreVersionTag() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setMavenProject(new MavenProject());
            this.getMojo().setIgnoreVersionTag(false);
            this.getMojo().setVersionBuild("1");
            this.getMojo().setCategory("unit");
            this.getMojo().setMarker("marker");
            this.getMojo().setPlatform("Linux");

            this.getMojo().execute();

            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("dtTestrunID"), is(EXAMPLE_TEST_RUN_ID));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testStartTestWithoutIgnoreVersionTagWithFallback() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setMavenProject(new MavenProject());
            this.getMojo().setIgnoreVersionTag(false);
            this.getMojo().setCategory("unit");

            this.getMojo().execute();

            assertThat(this.getMojo().getMavenProject().getProperties().getProperty("dtTestrunID"), is(EXAMPLE_TEST_RUN_ID));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testStartTestCategoryNotSet() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setMavenProject(new MavenProject());
            this.getMojo().setIgnoreVersionTag(true);
            this.getMojo().setVersionBuild("1");

            this.getMojo().execute();

            fail("Exception should be thrown - category is not set");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));
        }
    }


    @Test
    public void testStartTestIgnoreVersionTagWithException() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setMavenProject(new MavenProject());
            this.getMojo().setIgnoreVersionTag(true);
            this.getMojo().setCategory("unit");

            this.getMojo().execute();

            fail("Exception should be thrown - build version is not set");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));
        }
    }

    @Test
    public void testStartTestWithWrongBuildVersion() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setMavenProject(new MavenProject());
            this.getMojo().setIgnoreVersionTag(false);
            this.getMojo().setVersionBuild("");
            this.getMojo().setCategory("unit");

            this.getMojo().execute();

            fail("Exception should be thrown - build version is not set");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));
        }
    }

    @Test
    public void testStartTestProperties() throws Exception {
        this.applyFreshMojo();

        try {
            this.getMojo().setMavenProject(new MavenProject());

            this.getMojo().setVersionMajor("1");
            this.getMojo().setVersionMinor("2");
            this.getMojo().setVersionRevision("3");
            this.getMojo().setVersionMilestone("4");
            this.getMojo().setVersionBuild("5");
            this.getMojo().setIgnoreVersionTag(true);
            this.getMojo().setMarker("marker");
            this.getMojo().setCategory("unit");
            this.getMojo().setPlatform("Linux");

            Properties additionalProperties = new Properties();
            additionalProperties.setProperty("firstProperty", "firstValue");
            additionalProperties.setProperty("secondProperty", "secondValue");
            this.getMojo().setAdditionalProperties(additionalProperties);

            assertThat(this.getMojo().getVersionMajor(), is("1"));
            assertThat(this.getMojo().getVersionMinor(), is("2"));
            assertThat(this.getMojo().getVersionRevision(), is("3"));
            assertThat(this.getMojo().getVersionMilestone(), is("4"));
            assertThat(this.getMojo().getVersionBuild(), is("5"));
            assertThat(this.getMojo().isIgnoreVersionTag(), is(true));
            assertThat(this.getMojo().getMarker(), is("marker"));
            assertThat(this.getMojo().getCategory(), is("unit"));
            assertThat(this.getMojo().getPlatform(), is("Linux"));

            assertThat(this.getMojo().getAdditionalProperties().getProperty("firstProperty"), is("firstValue"));
            assertThat(this.getMojo().getAdditionalProperties().getProperty("secondProperty"), is("secondValue"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }
}