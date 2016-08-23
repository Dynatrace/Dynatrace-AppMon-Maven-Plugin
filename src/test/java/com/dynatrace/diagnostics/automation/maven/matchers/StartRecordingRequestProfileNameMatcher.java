package com.dynatrace.diagnostics.automation.maven.matchers;

import com.dynatrace.sdk.server.sessions.models.StartRecordingRequest;
import org.mockito.ArgumentMatcher;

public class StartRecordingRequestProfileNameMatcher extends ArgumentMatcher<StartRecordingRequest> {
    private String matchedSystemProfile;

    public StartRecordingRequestProfileNameMatcher(String matchedSessionName) {
        this.matchedSystemProfile = matchedSessionName;
    }

    @Override
    public boolean matches(Object request) {
        if (request instanceof StartRecordingRequest) {
            StartRecordingRequest startRecordingRequest = (StartRecordingRequest) request;

            return startRecordingRequest.getSystemProfile().equals(this.getMatchedSystemProfile());
        }

        return false;
    }

    public String getMatchedSystemProfile() {
        return matchedSystemProfile;
    }

    public void setMatchedSystemProfile(String matchedSystemProfile) {
        this.matchedSystemProfile = matchedSystemProfile;
    }
}
