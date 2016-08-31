# Dynatrace Maven Plugin

The automation plugin enables FULL Automation of Dynatrace by leveraging the REST interfaces of the Dynatrace AppMon Server. The automation plugin includes Maven tasks to execute the following actions on the Dynatrace AppMon Server:
* Activate Configuration: Activates a configuration within a system profile
* Clear Session: Clears the live session
* Enable/Disable Profile
* Get Agent Information: Either returns the number of connected agents or specific information about a single agent
* Create Memory/Thread Dumps: Triggers memory or thread dumps for a specific connected agent
* Reanalyze Stored Sessions: Triggers business transaction analysis of a stored session
* Restart Server/Collector
* Start/Stop Session Recording: Returns the actual recorded session name
* Start Test: returns testrun id, allowing to inject it into Dynatrace agent parameters

#### Table of Contents

* [Installation](#installation)  
 * [Prerequisites](#prerequisites)
 * [Manual Installation](#manual_installation)
* [Configuration](#configuration)
* [Available Maven goals](#goals)  
* [Additional Resources](#resources)

## <a name="installation"></a>Installation

### <a name="prerequisites"></a>Prerequisites

* Dynatrace Application Monitoring version: 6.3+
* Maven 3+

Find further information in the [Dynatrace community](https://community.dynatrace.com/community/display/DL/Automation+Library+%28Ant,+Maven%29+for+Dynatrace).

### <a name="manual_installation"></a>Manual Installation

* Download the [latest plugin]() and extract it into the `lib` folder in your project
* Import the Maven plugin into your local repository using the following command:
`mvn install:install-file -DgroupId=dynaTrace -DartifactId=dtAutomation -Dversion=6.5.0 -Dpackaging=maven-plugin -Dfile=dtAutomation-6.5.0.jar`
* Define properties for the Dynatrace goals as shown in pom.xml from the sample package
* Invoke your maven goals, e.g.: mvn dynaTrace:dtAutomation:6.2:startRecording

The Dynatrace maven plugin has the following identification: (pluginGroupId:pluginArtifactId:pluginVersion): dynaTrace:dtAutomation:6.5.0

## Building

In order to build the plugin, Maven environment is needed to be configured in your system. Then you should be able to build and install package by executing `mvn install`.
Jar file should be available in `target` folder

## <a name="configuration"></a>Configuration
A full example can be seen in the pom.xml as part of the project available in `examples` folder.

Using plugin properties:
```
<properties>
  <!-- Setting default values for Dynatrace Maven goals that operate on a system profile -->
  <dynaTrace.username>admin</dynaTrace.username>
  <dynaTrace.password>admin</dynaTrace.password>
  <dynaTrace.serverUrl>http://localhost:8020</dynaTrace.serverUrl>
  <dynaTrace.systemProfile>GoSpace</dynaTrace.systemProfile>
 
  <!-- This property will be used to store the actual Session Name for e.g.: Start/Stop Recording -->
  <dynaTrace.sessionNameProperty>dynaTrace.sessionName</dynaTrace.sessionNameProperty>
 
  <!-- Following is a list of properties for goal: startRecording -->
  <dynaTrace.sessionName>My Stored Session</dynaTrace.sessionName>
  <dynaTrace.sessionDescription>My stored Session Description</dynaTrace.sessionDescription>
  <dynaTrace.recordingOption>all</dynaTrace.recordingOption> <!-- other options: violations|timeseries -->
  <dynaTrace.sessionLocked>false</dynaTrace.sessionLocked>
  <dynaTrace.appendTimestamp>false</dynaTrace.appendTimestamp>
</properties>

```
Now we can call the startRecording goal in the following way:
`mvn dynaTrace:dtAutomation:startRecording`

You can inject the Dynatrace agent as part of surefire unit testing in Maven pom.xml with settings similar to this:
```
<plugin>
<groupId>org.apache.maven.plugins</groupId>
<artifactId>maven-surefire-plugin</artifactId>
<version>2.5</version>
<configuration>
<argLine>-agentpath:"C:\Program Files\dynaTrace\dynaTrace 6.5.0\agent\lib\dtagent.dll"=name=Maven,server=localhost:9998</argLine>
</configuration>
</plugin>
```

## <a name="tasks"></a>Available Maven tasks
Description of Available Maven Tasks

#### Server Management
* DtGetAgentInfo - Returns information about a connected Agent
* DtEnableProfile - Enables or disables a System Profile
* DtActivateConfiguration - Activates a Configuration of a System Profile
* DtRestartServer - Restarts a dynaTrace Server
* DtRestartCollector - Restarts a collector

#### Session Management
* DtClearSession - Clears the Live Session of a System Profile
* DtReanalyzeSession - Reanalyzes a stored session
* DtStartRecording - Starts session recording for a specified system profile
* DtStopRecording - Stops session recording for a specified system profile

#### Test Management
* DtStartTest - Sets meta data information for the Test Automation Feature and provides the DtStartTest.testRunId necessary to support parallel builds. The DtStartTest.testRunId value needs to be passed to the agent instrumenting the JVM that's executing the tests.
Resource Dumps
* DtMemoryDump - Creates a Memory Dump for an agent
* DtThreadDump - Creates a Thread Dump on an agent

## <a name="resources"></a>Additional Resources
- [Automation Library (Ant, Maven) for Dynatrace](https://community.dynatrace.com/community/display/DL/Automation+Library+%28Ant,+Maven%29+for+Dynatrace)
- [Test Automation and Maven](https://community.dynatrace.com/community/display/DOCDT63/Test+Automation+and+Maven)

- [Continuous Delivery & Test Automation](https://community.dynatrace.com/community/pages/viewpage.action?pageId=215161284)
- [Capture Performance Data from Tests](https://community.dynatrace.com/community/display/DOCDT63/Capture+Performance+Data+from+Tests)
- [Integrate Dynatrace in Continous Integration Builds](https://community.dynatrace.com/community/display/DOCDT63/Integrate+Dynatrace+in+Continuous+Integration+Builds)

