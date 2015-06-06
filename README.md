## Security

Describe the security approach taken for the API, and use these links to explain your decisions:

* https://auth0.com/blog/2014/01/07/angularjs-authentication-with-cookies-vs-token/
* https://auth0.com/blog/2014/01/27/ten-things-you-should-know-about-tokens-and-cookies/
* http://sitr.us/2011/08/26/cookies-are-bad-for-you.html

I would also store the keypair more securely in a production deployment; usually with the Infrastructure as Code.
## Building the application locally

To run just the unit tests, type `mvn test`.

> One of the unit tests takes several seconds to run as it waits for a future to time out. This test has been tagged as being `slow`.

To run the unit and integration tests (which are identified by their names ending with `IntegrationTest`), run

    mvn verify

The integration tests use an in-memory H2 database.

## Miscellaneous Notes