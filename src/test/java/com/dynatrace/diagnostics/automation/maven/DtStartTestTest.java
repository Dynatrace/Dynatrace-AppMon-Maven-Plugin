package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.servermanagement.ServerManagement;
import com.dynatrace.sdk.server.testautomation.TestAutomation;
import com.dynatrace.sdk.server.testautomation.models.CreateTestRunRequest;
import com.dynatrace.sdk.server.testautomation.models.TestCategory;
import com.dynatrace.sdk.server.testautomation.models.TestRun;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TestAutomation.class, DtStartTest.class})
public class DtStartTestTest {
    @Test
    public void startTestTest() throws Exception {
        DtStartTest startTest = spy(new DtStartTest());
        doReturn(null).when(startTest).getDynatraceClient();

        TestAutomation testAutomation = spy(new TestAutomation(null));
        TestRun testRun = new TestRun(0L, null, null, TestCategory.UNIT, "7f98a064-d00d-4224-8803-2f87f4988584", null, null, null, null, null, null, null, null, null, null, null, null, null);
        doReturn(testRun).when(testAutomation).createTestRun(Mockito.any(CreateTestRunRequest.class));
        whenNew(TestAutomation.class).withAnyArguments().thenReturn(testAutomation);


        startTest.setMavenProject(new MavenProject());
        startTest.setIgnoreVersionTag(true);
        startTest.setVersionBuild("1");


        try {
            startTest.execute();
            fail("Exception should be thrown - category is not set");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));
        }


        startTest.setCategory("unit");
        try {
            startTest.execute();
            assertThat(startTest.getMavenProject().getProperties().getProperty("dtTestrunID"), is("7f98a064-d00d-4224-8803-2f87f4988584"));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }
}