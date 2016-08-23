package com.dynatrace.diagnostics.automation.maven;

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
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TestAutomation.class, DtStartTest.class})
public class DtStartTestMojoTest extends AbstractDynatraceMojoTest<DtStartTest> {
    private static final String START_TEST_GOAL_NAME = "startTest";
    private static final String EXAMPLE_TEST_RUN_ID = "7f98a064-d00d-4224-8803-2f87f4988584";

    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.applyFreshMojo();

        TestRun testRun = new TestRun(0L, null, null, TestCategory.UNIT, EXAMPLE_TEST_RUN_ID, null, null, null, null, null, null, null, null, null, null, null, null, null);
        TestAutomation testAutomation = spy(new TestAutomation(mojo.getDynatraceClient()));

        /** define responses */
        doReturn(testRun).when(testAutomation).createTestRun(Mockito.any(CreateTestRunRequest.class));

        whenNew(TestAutomation.class).withAnyArguments().thenReturn(testAutomation);

        /** verify default values */
        assertThat(mojo.isIgnoreVersionTag(), is(false));
    }

    @Override
    protected String getMojoGoalName() {
        return START_TEST_GOAL_NAME;
    }

    @Test
    public void testStartTestWithIgnoreVersionTag() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setMavenProject(new MavenProject());
            mojo.setIgnoreVersionTag(true);
            mojo.setVersionBuild("1");
            mojo.setCategory("unit");

            mojo.execute();

            assertThat(mojo.getMavenProject().getProperties().getProperty("dtTestrunID"), is(EXAMPLE_TEST_RUN_ID));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testStartTestWithoutIgnoreVersionTag() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setMavenProject(new MavenProject());
            mojo.setIgnoreVersionTag(false);
            mojo.setVersionBuild("1");
            mojo.setCategory("unit");

            mojo.execute();

            assertThat(mojo.getMavenProject().getProperties().getProperty("dtTestrunID"), is(EXAMPLE_TEST_RUN_ID));
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testStartTestCategoryNotSet() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setMavenProject(new MavenProject());
            mojo.setIgnoreVersionTag(true);
            mojo.setVersionBuild("1");

            mojo.execute();

            fail("Exception should be thrown - category is not set");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));
        }
    }


    @Test
    public void testStartTestIgnoreVersionTagWithException() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setMavenProject(new MavenProject());
            mojo.setIgnoreVersionTag(true);
            mojo.setCategory("unit");

            mojo.execute();

            fail("Exception should be thrown - build version is not set");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));
        }
    }

    @Test
    public void testStartTestWithWrongBuildVersion() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setMavenProject(new MavenProject());
            mojo.setIgnoreVersionTag(false);
            mojo.setVersionBuild("");
            mojo.setCategory("unit");

            mojo.execute();

            fail("Exception should be thrown - build version is not set");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));
        }
    }

    @Test
    public void testStartTestProperties() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setMavenProject(new MavenProject());

            mojo.setVersionMajor("1");
            mojo.setVersionMinor("2");
            mojo.setVersionRevision("3");
            mojo.setVersionMilestone("4");
            mojo.setVersionBuild("5");
            mojo.setIgnoreVersionTag(true);
            mojo.setMarker("marker");
            mojo.setCategory("unit");
            mojo.setPlatform("Linux");

            assertThat(mojo.getVersionMajor(), is("1"));
            assertThat(mojo.getVersionMinor(), is("2"));
            assertThat(mojo.getVersionRevision(), is("3"));
            assertThat(mojo.getVersionMilestone(), is("4"));
            assertThat(mojo.getVersionBuild(), is("5"));
            assertThat(mojo.isIgnoreVersionTag(), is(true));
            assertThat(mojo.getMarker(), is("marker"));
            assertThat(mojo.getCategory(), is("unit"));
            assertThat(mojo.getPlatform(), is("Linux"));

        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }
}