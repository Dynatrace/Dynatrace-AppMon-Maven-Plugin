package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.exceptions.ServerResponseException;
import com.dynatrace.sdk.server.sessions.Sessions;
import com.dynatrace.sdk.server.sessions.models.RecordingOption;
import com.dynatrace.sdk.server.sessions.models.StartRecordingRequest;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * @goal startRecording
 * @phase pre-integration-test
 */
public class DtStartRecording extends DtServerProfileBase {

	/**
	 * @parameter expression="${dynaTrace.sessionName}"
	 * @required
	 */
	private String sessionName;

	/**
	 * @parameter expression="${dynaTrace.sessionDescription}"
	 */
	private String sessionDescription;

	/**
	 * @parameter default-value="all"  expression="${dynaTrace.recordingOption}"
	 * @required
	 */
	private String recordingOption;

	/**
	 * @parameter expression="${dynaTrace.sessionNameProperty}"
	 */
	private String sessionNameProperty;

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

		StartRecordingRequest startRecordingRequest = new StartRecordingRequest(this.getProfileName());
		startRecordingRequest.setPresentableName(this.getSessionName());
		startRecordingRequest.setDescription(this.getSessionDescription());
		startRecordingRequest.setSessionLocked(this.isSessionLocked());
		startRecordingRequest.setTimestampAllowed(this.isAppendTimestamp());

		if (this.getRecordingOption() != null) {
			startRecordingRequest.setRecordingOption(RecordingOption.fromInternal(this.getRecordingOption()));
		}

		try {
			String sessionName = sessions.startRecording(startRecordingRequest);
			getLog().info("Started recording on " + getProfileName() + " with SessionName " + sessionName); //$NON-NLS-1$ //$NON-NLS-2$

			if(sessionNameProperty != null && sessionNameProperty.length() > 0) {
				mavenProject.getProperties().setProperty(sessionNameProperty, sessionName);
			}
		} catch (ServerConnectionException | ServerResponseException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}

	public String getSessionName() {
		return sessionName;
	}

	public void setSessionDescription(String sessionDescription) {
		this.sessionDescription = sessionDescription;
	}

	public String getSessionDescription() {
		return sessionDescription;
	}

	public void setRecordingOption(String recordingOption) {
		this.recordingOption = recordingOption;
	}

	public String getRecordingOption() {
		return recordingOption;
	}

	public void setSessionLocked(boolean sessionLocked) {
		this.sessionLocked = sessionLocked;
	}

	public boolean isSessionLocked() {
		return sessionLocked;
	}

	public void setAppendTimestamp(boolean appendTimestamp) {
		this.appendTimestamp = appendTimestamp;
	}

	public boolean isAppendTimestamp() {
		return appendTimestamp;
	}

	public void setSessionNameProperty(String sessionNameProperty) {
		this.sessionNameProperty = sessionNameProperty;
	}

	public String getSessionNameProperty() {
		return sessionNameProperty;
	}
}
