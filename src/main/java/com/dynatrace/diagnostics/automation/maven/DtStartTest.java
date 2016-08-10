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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.dynatrace.diagnostics.automation.common.DtStartTestCommon;
/**
 * @goal startTest
 * @phase verify
 * @author cwpl-knecel
 * @author cwat-ruttenth
 * @author cwpl-mpankows
 */
public class DtStartTest extends DtServerProfileBase {

	/** @property expression="${dynaTrace.versionMajor}" */
	private String versionMajor;

	/** @property expression="${dynaTrace.versionMinor}" */
	private String versionMinor;

	/** @property expression="${dynaTrace.versionRevision}" */
	private String versionRevision;

	/** @property expression="${dynaTrace.ignoreVersionTag}" */
	private boolean ignoreVersionTag;

	/**
	 * Not supported since dT 6.2.
	 *
	 * @property expression="${dynaTrace.agentGroup}"
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private String agentGroup;

	/** @property expression="${dynaTrace.versionMilestone}" */
	private String versionMilestone;

	/** @property expression="${dynaTrace.versionBuild}" */
	private String versionBuild;

	/** @property expression="${dynaTrace.marker}" */
	private String marker;

	/** @property expression=${dynaTrace.additionalProperties}" (not used) */
	private Properties additionalProperties = new Properties();

	/** @property expression=${dynaTrace.testCategory}" */
	private String category;

	/** @property expression=${dynaTrace.loadTestName}" */
	private String loadTestName;

	/** @property expression=${dynaTrace.platform}" */
	private String platform;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		checkParameters();
		initVersionNumbers();
		final HashMap<String, String> additionalInformation = new HashMap<String, String>();
		for (Entry<Object, Object> entry : additionalProperties.entrySet()) {
			additionalInformation.put(entry.getKey().toString(), entry.getValue().toString());
		}
		getLog().info(DtStartTestCommon.generateInfoMessage(getProfileName(), versionMajor, versionMinor, versionRevision,
				versionBuild, versionMilestone, marker, category, loadTestName, platform, additionalInformation));
		// set TestMetaData via REST endpoint
		String testrunUUID = getEndpoint().startTest(getProfileName(), versionMajor,
				versionMinor, versionRevision, versionBuild, versionMilestone,
				marker, category, platform, loadTestName, additionalInformation);
		Properties props = mavenProject.getProperties();
		props.setProperty(DtStartTestCommon.TESTRUN_ID_PROPERTY_NAME, testrunUUID);
		getLog().info(MessageFormat.format(DtStartTestCommon.TESTRUN_ID_PROPERTY_MESSAGE, testrunUUID));
	}

	private void checkParameters() throws MojoFailureException {
		if (category == null) {
			throw new MojoFailureException(DtStartTestCommon.MISSING_CATEGORY_MESSAGE);
		}
		if (!DtStartTestCommon.TEST_CATEGORIES.contains(category)) {
			throw new MojoFailureException(MessageFormat.format(DtStartTestCommon.INVALID_CATEGORY_MESSAGE, category));
		}
		if (category != null && DtStartTestCommon.TEST_CATEGORY_LOAD.equalsIgnoreCase(category) && loadTestName == null) {
			throw new MojoFailureException(DtStartTestCommon.MISSING_LOAD_TEST_NAME_MESSAGE);
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
