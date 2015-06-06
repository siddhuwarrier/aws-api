## Building the application locally

To run just the unit tests, type `mvn test`.

> One of the unit tests takes several seconds to run as it waits for a future to time out. This test has been tagged as being `slow`.

To run the unit and integration tests (which are identified by their names ending with `IntegrationTest`), run

    mvn verify

The integration tests use:
* an embedded Redis server, and
* an in-memory H2 database.

## Miscellaneous Notes

### ESAPI

The Enterprise Security API (ESAPI) from the Open Web Application Security Project (OWASP) provides several convenient
abstractions for performing common cryptographic tasks, and its use was heavily encouraged by CWS's Security researchers.

However, the way logging is designed is interesting to say the least. It expects the user to use log4j or java-logging, and
as I use neither of these in the project, it logs its complaints on to `stdout`.