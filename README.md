Ballerina Socket Library
===================

  [![Build](https://github.com/ballerina-platform/module-ballerina-socket/workflows/Build/badge.svg)](https://github.com/ballerina-platform/module-ballerina-socket/actions?query=workflow%3ABuild)
  [![Daily build](https://github.com/ballerina-platform/module-ballerina-socket/workflows/Daily%20build/badge.svg)](https://github.com/ballerina-platform/module-ballerina-socket/actions?query=workflow%3A%22Daily+build%22)
  [![GitHub Last Commit](https://img.shields.io/github/last-commit/ballerina-platform/module-ballerina-socket.svg)](https://github.com/ballerina-platform/module-ballerina-socket/commits/master)
  [![Github issues](https://img.shields.io/github/issues/ballerina-platform/ballerina-standard-library/module/socket.svg?label=Open%20Issues)](https://github.com/ballerina-platform/ballerina-standard-library/labels/module%2Fsocket)
  [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

The Socket library is one of the standard library modules of the<a target="_blank" href="https://ballerina.io/"> Ballerina</a> language.

This module provides an implementation for sending/receiving messages to/from another application process (local or remote) for both connection-oriented and connectionless protocols. 

For more information on the operations supported go to [The Socket Module](https://ballerina.io/swan-lake/learn/api-docs/ballerina/socket/).

For example demonstrations of the usage, go to [Ballerina By Examples](https://ballerina.io/swan-lake/learn/by-example/tcp-socket-listener-client.html).

## `Issues` and `Projects`

`Issues` and `Project` tabs are disabled for this repository as this is one of the Ballerina Standard Libraries. To report bugs, request new features, start new discussions, view project boards, etc. please visit Ballerina Standard Library [parent repository](https://github.com/ballerina-platform/ballerina-standard-library).

This repository only contains the source code for the module.

## Building from the Source

### Setting Up the Prerequisites

1. Download and install Java SE Development Kit (JDK) version 11 (from one of the following locations).

   * [Oracle](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
   
   * [OpenJDK](https://adoptopenjdk.net/)
   
        > **Note:** Set the JAVA_HOME environment variable to the path name of the directory into which you installed JDK.
     

### Building the Source

Execute the commands below to build from source.

1. To build the library:
        
        ./gradlew clean build

2. To build the module without the tests:

        ./gradlew clean build -x test
        
## Contributing to Ballerina

As an open source project, Ballerina welcomes contributions from the community. 

For more information, go to the [contribution guidelines](https://github.com/ballerina-platform/ballerina-lang/blob/master/CONTRIBUTING.md).

## Code of Conduct

All contributors are encouraged to read the [Ballerina Code of Conduct](https://ballerina.io/code-of-conduct).

## Useful Links

* Discuss about code changes of the Ballerina project in [ballerina-dev@googlegroups.com](mailto:ballerina-dev@googlegroups.com).
* Chat live with us via our [Slack channel](https://ballerina.io/community/slack/).
* Post all technical questions on Stack Overflow with the [#ballerina](https://stackoverflow.com/questions/tagged/ballerina) tag.
