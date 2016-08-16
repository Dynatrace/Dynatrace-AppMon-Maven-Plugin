package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.exceptions.ServerResponseException;
import com.dynatrace.sdk.server.memorydumps.MemoryDumps;
import com.dynatrace.sdk.server.memorydumps.models.AgentPattern;
import com.dynatrace.sdk.server.memorydumps.models.JobState;
import com.dynatrace.sdk.server.memorydumps.models.MemoryDumpJob;
import com.dynatrace.sdk.server.memorydumps.models.StoredSessionType;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.net.URI;
import java.net.URISyntaxException;

@Mojo(name = "memoryDump", defaultPhase = LifecyclePhase.VERIFY)
public class DtMemoryDump extends DtAgentBase {

	/* FIXME - used new default value! */
	@Parameter(property = "dynaTrace.dumpType", defaultValue = "memdump_simple")
	private String dumpType;

	@Parameter(property = "dynaTrace.sessionLocked", defaultValue = "true")
	private boolean sessionLocked;

	@Parameter(property = "dynaTrace.memoryDumpNameProperty")
	private String memoryDumpNameProperty;

	@Parameter(property = "dynaTrace.waitForDumpTimeout", defaultValue = "60000")
	private int waitForDumpTimeout = 60000;

	@Parameter(property = "dynaTrace.waitForDumpPollingInterval", defaultValue = "5000")
	private int waitForDumpPollingInterval = 5000;

	@Parameter(property = "dynaTrace.dumpStatusProperty")
	private String dumpStatusProperty;

	@Parameter(property = "dynaTrace.doGc", defaultValue = "false")
	private boolean doGc;

	@Parameter(property = "dynaTrace.autoPostProcess", defaultValue = "false")
	private boolean autoPostProcess;

	@Parameter(property = "dynaTrace.capturePrimitives", defaultValue = "false")
	private boolean capturePrimitives;

	@Parameter(property = "dynaTrace.captureStrings", defaultValue = "false")
	private boolean captureStrings;

	@Override
	public void execute() throws MojoExecutionException {
		System.out.println("Creating Memory Dump for " + getProfileName() + "-" + getAgentName() + "-" + getHostName() + "-" + getProcessId()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		MemoryDumps memoryDumps = new MemoryDumps(this.getDynatraceClient());

		MemoryDumpJob memoryDumpJob = new MemoryDumpJob();
		memoryDumpJob.setAgentPattern(new AgentPattern(this.getAgentName(), this.getHostName(), this.getProcessId()));
		memoryDumpJob.setSessionLocked(this.isSessionLocked());
		memoryDumpJob.setCaptureStrings(this.getCaptureStrings());
		memoryDumpJob.setCapturePrimitives(this.getCapturePrimitives());
		memoryDumpJob.setPostProcessed(this.getAutoPostProcess());
		memoryDumpJob.setDogc(this.getDoGc());

		if (this.getDumpType() != null) {
			memoryDumpJob.setStoredSessionType(StoredSessionType.fromInternal(this.getDumpType())); /* TODO FIXME - dump type is wrong? use new values with prefixes! */
		}

		try {
			String memoryDumpLocation = memoryDumps.createMemoryDumpJob(this.getProfileName(), memoryDumpJob);

			URI uri = new URI(memoryDumpLocation);
			String[] uriPathArray = uri.getPath().split("/");

			String memoryDump = null;

			try {
				memoryDump = uriPathArray[uriPathArray.length - 1];
			} catch (Exception e) {
				throw new MojoExecutionException("Malformed memory dump response", new Exception()); //$NON-NLS-1$
			}

			if (memoryDumpNameProperty != null && memoryDumpNameProperty.length() > 0) {
				mavenProject.getProperties().setProperty(memoryDumpNameProperty, memoryDump);
			}

			if (memoryDump == null || memoryDump.length() == 0) {
				throw new MojoExecutionException("Memory Dump wasnt taken"); //$NON-NLS-1$
			}

			int timeout = waitForDumpTimeout;

			JobState memoryDumpJobState = memoryDumps.getMemoryDumpJob(this.getProfileName(), memoryDump).getState();
			boolean dumpFinished = memoryDumpJobState.equals(JobState.FINISHED) || memoryDumpJobState.equals(JobState.FAILED);

			while (!dumpFinished && (timeout > 0)) {
				try {
					java.lang.Thread.sleep(waitForDumpPollingInterval);
					timeout -= waitForDumpPollingInterval;
				} catch (InterruptedException e) {
				}

				memoryDumpJobState = memoryDumps.getMemoryDumpJob(this.getProfileName(), memoryDump).getState();
				dumpFinished = memoryDumpJobState.equals(JobState.FINISHED) || memoryDumpJobState.equals(JobState.FAILED);
			}

			if (dumpStatusProperty != null && dumpStatusProperty.length() > 0) {
				mavenProject.getProperties().setProperty(dumpStatusProperty, String.valueOf(dumpFinished));
			}
		} catch (ServerResponseException e) {
			this.getLog().error(String.format("Cannot take memory dump: %s", e.getMessage()));
		} catch (ServerConnectionException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (URISyntaxException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	private boolean getCaptureStrings() {
		return captureStrings;
	}

	private boolean getCapturePrimitives() {
		return capturePrimitives;
	}

	private boolean getAutoPostProcess() {
		return autoPostProcess;
	}

	private boolean getDoGc() {
		return doGc;
	}

	public void setDoGc(boolean doGc) {
		this.doGc = doGc;
	}

	public void setAutoPostProcess(boolean autoPostProcess) {
		this.autoPostProcess = autoPostProcess;
	}

	public void setCapturePrimitives(boolean capturePrimitives) {
		this.capturePrimitives = capturePrimitives;
	}

	public void setCaptureStrings(boolean captureStrings) {
		this.captureStrings = captureStrings;
	}

	public void setDumpType(String dumpType) {
		this.dumpType = dumpType;
	}

	public String getDumpType() {
		return dumpType;
	}

	public void setSessionLocked(boolean sessionLocked) {
		this.sessionLocked = sessionLocked;
	}

	public boolean isSessionLocked() {
		return sessionLocked;
	}

	public void setMemoryDumpNameProperty(String memoryDumpNameProperty) {
		this.memoryDumpNameProperty = memoryDumpNameProperty;
	}

	public String getMemoryDumpNameProperty() {
		return memoryDumpNameProperty;
	}

	public void setWaitForDumpTimeout(int waitForDumpTimeout) {
		this.waitForDumpTimeout = waitForDumpTimeout;
	}

	public int getWaitForDumpTimeout() {
		return waitForDumpTimeout;
	}

	public void setWaitForDumpPollingInterval(int waitForDumpPollingInterval) {
		this.waitForDumpPollingInterval = waitForDumpPollingInterval;
	}

	public int getWaitForDumpPollingInterval() {
		return waitForDumpPollingInterval;
	}

	public void setDumpStatusProperty(String dumpStatusProperty) {
		this.dumpStatusProperty = dumpStatusProperty;
	}

	public String getDumpStatusProperty() {
		return dumpStatusProperty;
	}
}
