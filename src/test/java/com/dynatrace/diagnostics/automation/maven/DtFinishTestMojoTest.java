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
import com.dynatrace.sdk.server.testautomation.models.TestRun;

import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.*;

import java.util.Properties;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TestAutomation.class, DtFinishTest.class, Properties.class})
public class DtFinishTestMojoTest extends AbstractDynatraceMojoTest<DtFinishTest> {
    private static final String TESTRUN_ID_PROPERTY_NAME = "finishTest";
    private static final String EXAMPLE_TEST_RUN_ID = "7f98a064-d00d-4224-8803-2f87f4988584";
    private static final String EXAMPLE_PROFILE = "Test";

    TestAutomation testAutomation;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.applyFreshMojo();

        testAutomation = spy(new TestAutomation(this.getMojo().getDynatraceClient()));

        /** define responses */
        doReturn(mock(TestRun.class)).when(testAutomation).finishTestRun(Mockito.any(String.class), Mockito.any(String.class));
        whenNew(TestAutomation.class).withAnyArguments().thenReturn(testAutomation);
    }

    @Override
    protected String getMojoGoalName() {
        return TESTRUN_ID_PROPERTY_NAME;
    }

    @Test
    public void finishTestRunShouldBeExecuted() throws Exception {
        this.applyFreshMojo();
        this.getMojo().setTestRunId(EXAMPLE_TEST_RUN_ID);
        this.getMojo().setProfileName(EXAMPLE_PROFILE);

        this.getMojo().execute();

        Mockito.verify(testAutomation).finishTestRun(eq(EXAMPLE_PROFILE), eq(EXAMPLE_TEST_RUN_ID));
    }

    @Test(expected = IllegalArgumentException.class)
    public void finishTestRunWithNoTestRunIDAndProfileNameShouldFail() throws Exception {
        this.applyFreshMojo();

        this.getMojo().execute();
    }

    @Test(expected = IllegalArgumentException.class)
    public void finishTestRunWithNoTestRunIDShouldFail() throws Exception {
        this.applyFreshMojo();
        this.getMojo().setProfileName(EXAMPLE_PROFILE);

        this.getMojo().execute();
    }

    @Test(expected = IllegalArgumentException.class)
    public void finishTestRunWithNoProfileNameShouldFail() throws Exception {
        this.applyFreshMojo();
        this.getMojo().setTestRunId(EXAMPLE_TEST_RUN_ID);

        this.getMojo().execute();
    }

}