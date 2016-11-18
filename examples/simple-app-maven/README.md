## Example project with Dynatrace Maven Plugin

This project contains example usage of the Dynatrace Maven Plugin.

### Prerequisites

Maven plugin should be built or installed before running this project.

### Running project

To build and install project using Maven, simply execute: `mvn install`.
To run tests with injected agent, execute: `mvn test`

### Running tasks

In order to run any Dynatrace Maven Plugin goal, `mvn dynaTrace:dtAutomation:YOUR_GOAL_NAME`. (e.g. `mvn dynaTrace:dtAutomation:enableProfile`, `mvn dynaTrace:dtAutomation:getAgentInfo`).

In the `pom-start-test.xml` there is an example of usage **startTest** goal.
In the `pom.xml` two goals are used, **startTest** and **finishTest**.

If you want to try simple version of build script (`pom-start-test.xml`) with example usage of the startTest goal injected into test, use `mvn -f pom-start-test.xml test`.
