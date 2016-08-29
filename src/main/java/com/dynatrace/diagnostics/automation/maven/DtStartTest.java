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

import com.dynatrace.diagnostics.automation.util.DtUtil;
import com.dynatrace.sdk.server.testautomation.TestAutomation;
import com.dynatrace.sdk.server.testautomation.models.CreateTestRunRequest;
import com.dynatrace.sdk.server.testautomation.models.TestCategory;
import com.dynatrace.sdk.server.testautomation.models.TestMetaData;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

/**
 * Implements "startTest" Maven goal
 */
@Mojo(name = "startTest", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class DtStartTest extends DtServerProfileBase {

    /* Attributes and properties names  */
    public static final String ATTRIBUTE_TEST_BUILD = "versionBuild";
    public static final String ATTRIBUTE_TEST_CATEGORY = "testCategory";
    public static final String TESTRUN_ID_PROPERTY_NAME = "dtTestrunID";
    public static final String DT_AGENT_TESTRUN_OPTION = "optionTestRunIdJava";

    /* Messages */
    private static final String INVALID_BUILD_NUMBER_MESSAGE = "Build number cannot be '-' or empty";
    private static final String MISSING_BUILD_MESSAGE = "Task requires attribute \"" + ATTRIBUTE_TEST_BUILD + "\"";
    private static final String INVALID_CATEGORY_MESSAGE = "\"" + ATTRIBUTE_TEST_CATEGORY + "\" has invalid value \"{0}\"." +
            "Select one from " + Arrays.toString(DtUtil.getTestCategoryInternalValues());
    private static final String TESTRUN_ID_PROPERTY_MESSAGE = "Setting property <" + TESTRUN_ID_PROPERTY_NAME + "> to value <{0}>. " +
            "Remember to pass it to DT agent in <" + DT_AGENT_TESTRUN_OPTION + "> parameter";

    private static final String INDENTATION_WITH_NEW_LINE = "\n\t";
    private static final String DEEP_INDENTATION_WITH_NEW_LINE = "\n\t\t";

    /* Properties with default values available in Maven Project environment */
    @Parameter(property = "dynaTrace.versionMajor")
    private String versionMajor;

    @Parameter(property = "dynaTrace.versionMinor")
    private String versionMinor;

    @Parameter(property = "dynaTrace.versionRevision")
    private String versionRevision;

    @Parameter(property = "dynaTrace.ignoreVersionTag", defaultValue = "false")
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

    @Parameter(property = "dynaTrace.additionalProperties")
    private Properties additionalProperties = new Properties();

    /**
     * Executes maven goal
     *
     * @throws MojoExecutionException whenever connecting to the server, parsing a response or execution fails
     * @throws MojoFailureException   whenever provided properties are invalid
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            this.checkParameters();
            this.initVersionNumbers();

            this.getLog().info(this.generateInfoMessage());

            TestAutomation testAutomation = new TestAutomation(this.getDynatraceClient());
            String testRunUUID = testAutomation.createTestRun(this.buildTestRunRequest()).getId();

            Properties properties = this.getMavenProject().getProperties();
            properties.setProperty(TESTRUN_ID_PROPERTY_NAME, testRunUUID);

            this.getLog().info(MessageFormat.format(TESTRUN_ID_PROPERTY_MESSAGE, testRunUUID));
        } catch (Exception e) {
            throw new MojoExecutionException(String.format("Exception when executing: %s", e.getMessage()), e);
        }
    }

    /**
     * Validates provided parameters
     *
     * @throws MojoFailureException whenever provided properties are invalid
     */
    private void checkParameters() throws MojoFailureException {
        if (this.ignoreVersionTag && (this.versionBuild == null)) {
            throw new MojoFailureException(MISSING_BUILD_MESSAGE);
        }

        try {
            TestCategory.fromInternal(this.category);
        } catch (IllegalArgumentException e) {
            throw new MojoFailureException(MessageFormat.format(INVALID_CATEGORY_MESSAGE, this.category));
        }
    }

    /**
     * Initializes version information
     * <p>
     * By default, version information is extracted from the <version> tag in pom
     * if {@code ignoreVersionTag} flag is set to {@code true},
     * we take version information from appropriate attributes
     *
     * @throws MojoFailureException when build version is invalid
     */
    private void initVersionNumbers() throws MojoFailureException {
        if (!this.ignoreVersionTag) {
            this.getLog().info("Using version from pom <version> tag");

            DefaultArtifactVersion version = new DefaultArtifactVersion(this.getMavenProject().getVersion());
            this.versionMilestone = version.getQualifier();
            this.versionMajor = String.valueOf(version.getMajorVersion());
            this.versionMinor = String.valueOf(version.getMinorVersion());
            this.versionRevision = String.valueOf(version.getIncrementalVersion());

            /** DefaultArtifactVersion will provide 0 as build even if it's not explicitly provided */
            if ((this.versionBuild == null) || (version.getBuildNumber() != 0)) {
                this.versionBuild = String.valueOf(version.getBuildNumber());
            } else {
                this.getLog().debug("Build number extracted from pom version is empty, falling back to the value provided in parameters.");
            }
        } else {
            /** no need to do anything, fields tied to parameters are initialized automatically */
            this.getLog().info("Ignoring pom version, using configuration parameters to set version information");
        }

        if (!this.isBuildNumberValid(versionBuild)) {
            throw new MojoFailureException(INVALID_BUILD_NUMBER_MESSAGE);
        }
    }

    /**
     * Validates build number
     *
     * @param buildNumber - build version to check
     * @return boolean {@code true} when build number has proper format, {@code false} otherwise
     */
    private boolean isBuildNumberValid(String buildNumber) {
        if (buildNumber == null || "".equals(buildNumber.trim())) {
            return false;
        }

        return !"-".equals(buildNumber);
    }

    /**
     * Creates test run request required for {@code TestAutomation#createTestRun} method
     *
     * @return {@CreateTestRunRequest} that contains configured request
     */
    private CreateTestRunRequest buildTestRunRequest() {
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

        TestMetaData testMetaData = new TestMetaData();

        for (Map.Entry<Object, Object> property : this.additionalProperties.entrySet()) {
            testMetaData.setValue(property.getKey().toString(), property.getValue().toString());
        }

        request.setAdditionalMetaData(testMetaData);

        return request;
    }

    /**
     * Returns a debug message with all the details regarding provided test run information
     *
     * @return {@String} that contains formatted log message with test run request properties values
     */
    private String generateInfoMessage() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Setting Test Information for system profile: ").append(this.getProfileName());
        stringBuilder.append(INDENTATION_WITH_NEW_LINE).append("version: ").append(this.versionMajor).append(".").append(this.versionMinor).append(".").
                append(this.versionRevision).append(".").append(this.versionBuild).append(" milestone: ").append(this.versionMilestone);

        if (!DtUtil.isEmpty(this.marker)) {
            stringBuilder.append(INDENTATION_WITH_NEW_LINE).append("marker: ").append(this.marker);
        }

        if (!DtUtil.isEmpty(this.category)) {
            stringBuilder.append(INDENTATION_WITH_NEW_LINE).append("category: ").append(this.category);
        }

        if (!DtUtil.isEmpty(this.platform)) {
            stringBuilder.append(INDENTATION_WITH_NEW_LINE).append("platform: ").append(this.platform);
        }

        if (!this.additionalProperties.isEmpty()) {
            stringBuilder.append(INDENTATION_WITH_NEW_LINE).append("custom properties: ");

            for (Map.Entry<Object, Object> property : this.additionalProperties.entrySet()) {
                stringBuilder.append(DEEP_INDENTATION_WITH_NEW_LINE).append(property.getKey()).append("=").append(property.getValue());
            }
        }

        return stringBuilder.toString();
    }

    public String getVersionMajor() {
        return this.versionMajor;
    }

    public void setVersionMajor(String versionMajor) {
        this.versionMajor = versionMajor;
    }

    public String getVersionMinor() {
        return this.versionMinor;
    }

    public void setVersionMinor(String versionMinor) {
        this.versionMinor = versionMinor;
    }

    public String getVersionRevision() {
        return this.versionRevision;
    }

    public void setVersionRevision(String versionRevision) {
        this.versionRevision = versionRevision;
    }

    public boolean isIgnoreVersionTag() {
        return this.ignoreVersionTag;
    }

    public void setIgnoreVersionTag(boolean ignoreVersionTag) {
        this.ignoreVersionTag = ignoreVersionTag;
    }

    public String getVersionMilestone() {
        return this.versionMilestone;
    }

    public void setVersionMilestone(String versionMilestone) {
        this.versionMilestone = versionMilestone;
    }

    public String getVersionBuild() {
        return this.versionBuild;
    }

    public void setVersionBuild(String versionBuild) {
        this.versionBuild = versionBuild;
    }

    public String getMarker() {
        return this.marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPlatform() {
        return this.platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Properties getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperties(Properties additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
}
