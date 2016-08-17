# Dynatrace Automation Library for Maven

The automation library enables FULL Automation of Dynatrace by leveraging the REST interfaces of the Dynatrace Server. The automation library includes Maven Goals to execute the following actions on the Dynatrace Server:
* Start/Stop Session Recording: Returns the actual recorded session name
* Set Test Information: Sets information about the tested built to be used by Test Automation Center
* Clear Session: Clears the live session
* Reanalyze Stored Sessions: Triggers business transaction analysis of a stored session
* Enable/Disable Profile
* Activate Configuration: Activates a configuration within a system profile
* Get Agent Information: Either returns the number of connected agents or specific information about a single agent
* Create Memory/Thread Dumps: Triggers memory or thread dumps for a specific connected agent
* Restart Server/Collector