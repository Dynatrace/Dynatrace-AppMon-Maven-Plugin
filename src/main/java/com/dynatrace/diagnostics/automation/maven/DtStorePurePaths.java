package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.exceptions.ServerResponseException;
import com.dynatrace.sdk.server.sessions.Sessions;
import com.dynatrace.sdk.server.sessions.models.RecordingOption;
import com.dynatrace.sdk.server.sessions.models.StoreSessionRequest;
import org.apache.maven.plugin.MojoExecutionException;

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
		Sessions sessions = new Sessions(this.getDynatraceClient());

		StoreSessionRequest storeSessionRequest = new StoreSessionRequest(this.getProfileName());
		storeSessionRequest.setSessionLocked(this.isSessionLocked());
		storeSessionRequest.setAppendTimestamp(this.isAppendTimestamp());

		if (this.getRecordingOption() != null) {
			storeSessionRequest.setRecordingOption(RecordingOption.fromInternal(this.getRecordingOption()));
		}

		try {
			sessions.store(storeSessionRequest);
		} catch (ServerConnectionException | ServerResponseException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
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
