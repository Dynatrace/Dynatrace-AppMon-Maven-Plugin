package com.dynatrace.diagnostics.automation.maven;

import org.apache.maven.plugin.MojoExecutionException;

import com.dynatrace.diagnostics.automation.maven.DtServerProfileBase;

public class DtStorePurePaths extends DtServerProfileBase{
	/**
	 * @parameter expression="${dynaTrace.recordingOption}"
	 */
	private String recordingOption;
	/**
	 * @parameter expression="${dynaTrace.sessionLocked}"
	 */
	private boolean sessionLocked;
	/**
	 * @parameter expression="${dynaTrace.appendTimestamp}"
	 */
	private boolean appendTimestamp;

	@Override
	public void execute() throws MojoExecutionException {
		getEndpoint().storePurePaths(getProfileName(), getRecordingOption(), isSessionLocked(), isAppendTimestamp());
	}

	public String getRecordingOption() {
		return recordingOption;
	}

	public void setRecordingOption(String recordingOption) {
		this.recordingOption = recordingOption;
	}

	public boolean isSessionLocked() {
		return sessionLocked;
	}

	public void setSessionLocked(boolean sessionLocked) {
		this.sessionLocked = sessionLocked;
	}

	public boolean isAppendTimestamp() {
		return appendTimestamp;
	}

	public void setAppendTimestamp(boolean appendTimestamp) {
		this.appendTimestamp = appendTimestamp;
	}
}
