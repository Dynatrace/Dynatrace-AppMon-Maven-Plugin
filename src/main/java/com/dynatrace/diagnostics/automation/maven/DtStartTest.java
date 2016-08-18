/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: DtStartTest.java
 * @date: 06.02.2013
 * @author: cwat-ruttenth
 * @date: 06.11.2014
 * @author: cwpl-knecel
 */
package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.diagnostics.automation.common.DtStartTestCommon;
import com.dynatrace.sdk.server.testautomation.TestAutomation;
import com.dynatrace.sdk.server.testautomation.models.CreateTestRunRequest;
import com.dynatrace.sdk.server.testautomation.models.TestCategory;
import com.dynatrace.sdk.server.testautomation.models.TestMetaData;
import com.dynatrace.sdk.server.testautomation.models.TestRun;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
/**
 * @author cwpl-knecel
 * @author cwat-ruttenth
 * @author cwpl-mpankows
 */

@Mojo(name = "startTest", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class DtStartTest extends DtServerProfileBase {

	@Parameter(property = "dynaTrace.versionMajor")
	private String versionMajor;

	@Parameter(property = "dynaTrace.versionMinor")
	private String versionMinor;

	@Parameter(property = "dynaTrace.versionRevision")
	private String versionRevision;

	@Parameter(property = "dynaTrace.ignoreVersionTag")
	private boolean ignoreVersionTag;

	@Parameter(property = "dynaTrace.versionMilestone")
	private String versionMilestone;

	@Parameter(property = "dynaTrace.versionBuild")
	private String versionBuild;

	@Parameter(property = "dynaTrace.marker")
	private String marker;

	@Parameter(property = "dynaTrace.testCategory", required = true)
	private String category;

	@Parameter(property = "dynaTrace.platform")
	private String platform;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			checkParameters();
			initVersionNumbers();

			getLog().info(DtStartTestCommon.generateInfoMessage(getProfileName(), versionMajor, versionMinor, versionRevision,
					versionBuild, versionMilestone, marker, category, platform, new HashMap<String, String>()));
			// set TestMetaData via REST endpoint

			TestAutomation testAutomation = new TestAutomation(this.getDynatraceClient());

			CreateTestRunRequest request = new CreateTestRunRequest();
			request.setSystemProfile(this.getProfileName());
			request.setVersionMajor(this.versionMajor);
			request.setVersionMinor(this.versionMinor);
			request.setVersionRevision(this.versionRevision);
			request.setVersionBuild(this.versionBuild);
			request.setVersionMilestone(this.versionMilestone);
			request.setMarker(this.marker);
			request.setCategory(TestCategory.fromInternal(this.category));
			request.setPlatform(this.platform);

			TestRun testRun = testAutomation.createTestRun(request);

			String testrunUUID = testRun.getId();

			Properties props = mavenProject.getProperties();
			props.setProperty(DtStartTestCommon.TESTRUN_ID_PROPERTY_NAME, testrunUUID);
			getLog().info(MessageFormat.format(DtStartTestCommon.TESTRUN_ID_PROPERTY_MESSAGE, testrunUUID));
		} catch (Exception e) {
			throw new MojoExecutionException("Exception when executing: " + e.getMessage(), e); //$NON-NLS-1$
		}
	}

	private void checkParameters() throws MojoFailureException {
		try {
			TestCategory testCategory = TestCategory.fromInternal(this.category);
		} catch(IllegalArgumentException e) {
			throw new MojoFailureException(MessageFormat.format(DtStartTestCommon.INVALID_CATEGORY_MESSAGE, this.category));
		}
	}

	private void initVersionNumbers() throws MojoFailureException {
		// by default version information is extracted from the <version> tag in pom
		// if ignoreVersionTag flag is set to "true", we take version information from
		// appropriate attributes
		if (ignoreVersionTag) {
			// no need to do anything, fields tied to parameters are initialized automatically
			getLog().info("Ignoring pom version, using configuration parameters to set version information"); //$NON-NLS-1$
			if (versionBuild == null) {
				throw new MojoFailureException(DtStartTestCommon.MISSING_BUILD_MESSAGE);
			}
		} else {
			getLog().info("Using version from pom <version> tag"); //$NON-NLS-1$
			DefaultArtifactVersion version = new DefaultArtifactVersion(mavenProject.getVersion());
			versionMajor = String.valueOf(version.getMajorVersion());
			versionMinor = String.valueOf(version.getMinorVersion());
			versionRevision = String.valueOf(version.getIncrementalVersion());
			// back up versionBuild provided in the plugin configuration parameter
			String versionBuildFromParameter = versionBuild;
			versionBuild = String.valueOf(version.getBuildNumber());
			// DefaultArtifactVersion will provide 0 as build even if it's not exlicitly provided
			if (versionBuild.equalsIgnoreCase("0") && versionBuildFromParameter != null) { //$NON-NLS-1$
				// try to fall back to build provided in the parameter
				getLog().debug(
						"Build number extracted from pom version is empty, falling back to the value provided in parameters."); //$NON-NLS-1$
				versionBuild = versionBuildFromParameter;
			}
			versionMilestone = version.getQualifier();
		}
		if (!DtStartTestCommon.isBuildNumberValid(versionBuild)) {
			throw new MojoFailureException(DtStartTestCommon.INVALID_BUILD_NUMBER_MESSAGE);
		}
	}
}
