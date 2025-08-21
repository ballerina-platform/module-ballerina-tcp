# Change Log
This file contains all the notable changes done to the Ballerina TCP package through the releases.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.13.1] - 2025-08-21

### Changed

- [Update Netty dependency version](https://github.com/ballerina-platform/ballerina-library/issues/8174)

## [1.13.0] - 2025-03-12

### Changed
- [Mark `writeBytes` and `readBytes` methods in the client as `isolated`](https://github.com/ballerina-platform/ballerina-library/issues/7609)

## [1.12.1] - 2025-02-11

### Fixed

- [Address Netty security vulnerabilities: `CVE-2025-24970` and `CVE-2025-25193`](https://github.com/ballerina-platform/ballerina-library/issues/7571)

## [1.12.0] - 2025-02-07

### Fixed
- [Address CVE-2024-7254 Protobuf Vulnerability](https://github.com/ballerina-platform/ballerina-library/issues/7013#event-14332816771)

## [1.11.0] - 2024-08-20

### Changed
- [Make some of the Java classes proper utility classes](https://github.com/ballerina-platform/ballerina-standard-library/issues/4941)

## [1.9.1] - 2023-10-12

### Fixed
- [Address netty vulnerability: CVE-2023-4586](https://github.com/ballerina-platform/ballerina-standard-library/issues/4908)

## [1.9.0] - 2023-09-15

- This version maintains the latest dependency versions.

## [1.8.0] - 2023-06-30

### Fixed
- [Address CVE-2023-34462 netty Vulnerability](https://github.com/ballerina-platform/ballerina-standard-library/issues/4599)

## [1.7.1] - 2023-06-01

- This version maintains the latest dependency versions.

## [1.5.0] - 2022-11-29

### Changed
- [API docs updated](https://github.com/ballerina-platform/ballerina-standard-library/issues/3463)

## [2201.0.0] - 2022-01-30

- [Add resource code snippet generation code action for tooling](https://github.com/ballerina-platform/ballerina-standard-library/issues/2034)

## [swan-lake-beta5] - 2021-12-01

## Changed
- [Mark TCP Service type as distinct](https://github.com/ballerina-platform/ballerina-standard-library/issues/2398)

### Added
- [Add an API to the Caller to get the unique connection id](https://github.com/ballerina-platform/ballerina-standard-library/issues/2131)
- [Make the tcp caller isolated](https://github.com/ballerina-platform/ballerina-standard-library/issues/2251)

## [1.2.0-beta.3] - 2021-10-10

### Added
- [Introduce write time out for TCP client](https://github.com/ballerina-platform/ballerina-standard-library/issues/1684)

## [1.2.0-beta.2] - 2021-07-07

### Fixed
- [Fix the secure client initialization failure when ciphers are not configured](https://github.com/ballerina-platform/ballerina-standard-library/issues/1569)

## [0.8.0-beta.1] - 2021-05-06

### Fixed
- [Fix the secure listener initialization failure when ciphers are not configured](https://github.com/ballerina-platform/ballerina-standard-library/issues/1367)
