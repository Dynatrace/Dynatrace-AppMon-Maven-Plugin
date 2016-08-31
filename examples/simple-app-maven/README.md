## Example project with Dynatrace Maven Plugin

This project contains example usage of the Dynatrace Maven Plugin.

### Prerequisites

Maven plugin should be built or installed before running this project

### Running project

To build and install project using Maven, simply execute: `mvn install`.
To run tests with injected agent, execute: `mvn test`

### Running tasks

In order to run any Dynatrace Maven Plugin goal, `mvn dynaTrace:dtAutomation:YOUR_GOAL_NAME`. (e.g. `mvn dynaTrace:dtAutomation:enableProfile`).
Every task usage is presented in `pom.xml` and `pom-start-test.xml' files.

If you want to try simple version of build script (`pom-start-test.xml`) with example usage of the startTest goal injected into test, use `mvn -p pom-start-test.xml test`
