The lockhart-aws-browser consists of two separate web applications:
* A backend REST API written in Scala using the [Scalatra](http://www.scalatra.org) framework, which provides endpoints for 
authentication and interacting with AWS.
* A frontend node.js single-page web application that uses AngularJS to query the backend API.

The source code for each of these applications is present as a separate git submodule. To clone the application, please 
do the following:

    git clone https://siddhuwarrier@bitbucket.org/siddhuwarrier/lockhart-aws-browser.git
    git submodule init
    git submodule update
    
The backend API source is in the `api` directory, while the frontend webapp source is in the `frontend` directory. Instructions
to run each of these applications is contained within the `README.md` files in its own directory. 

# Deployment


//add notes on deployment here.
# Notes on Design

## API <-> Client Interaction

I have used the [AWS Java SDK](http://aws.amazon.com/sdk-for-java/) to connect to AWS. The AWS credentials used can be 
configured in the following ways:

* Editing the entries in the `app.conf` file in `src/main/resources`.
* Using the environment variables `$AWS_ACCESS_KEY` and `$AWS_SECRET_ACCESS_KEY`.
    * If running on heroku, these variables can be set using the Heroku Toolbelt command `heroku config:set`.
    
The API returns a list of instances, which are then formatted by the front-end into a table. 

### Design Choices 
     
There is no AWS EC2 REST API — AWS supports SOAP (now deprecated) and a query API that appears to just use HTTP as a 
transport mechanism to deliver the request envelope to the server. Therefore, the only viable option to accessing this
API is to use the SDK. The SDK, however, does not appear to provide several features that would have allowed for its use
for server side pagination, including:
* No support for limiting the number of instances retrieved.
* No support for retrieving a subset of instances by count.

As a result, I have decided to implement the pagination entirely on the client-side. To wit,
* The frontend retrieves the full list of instances associated with an AWS account in a given region, and caches the result.
* Every request to change the page results in data being retrieved from the cache.

Therefore, to reload the data in the page to reflect changes on the server, the user needs to refresh the browser. This decision 
was made on account of the following factors:
* To minimise the amount of API calls made out to AWS from the server to re-retrieve the same data.
* To keep the API stateless.
* Time limitations.

## Security

All of the requests to the API are secured in production using HTTPS (piggybacking on Heroku's SSL support; all applications 
within the `*.herokuapp.com` domain can be accessed using HTTPS (with Heroku providing a wildcard SSL cert for free). This
mitigates against MITM attacks.

### Authentication

The application is secured using a simple user/password authentication scheme that is backed by a PostGreSQL database. The
 password is stored as a salted (SHA-512) hash.

Once a user is authenticated, he/she receives a JSON Web Token(JWT) token that he/she needs to place inside a `Bearer` token within the HTTP
 `Authorization` header.
 
Some of the reasons I chose to go with a token-based authentication approach over using cookies are:
 * Better CORS support: The frontend API runs on a different (sub)domain (of herokuapp.com) in production, and therefore the API
 has to support CORS requests.
 * CSRF: CSRF protection no longer required.
 * Standards-based: [JWT](http://bit.ly/1B0zHPH) and 
 Javascript Object Signing and Encryption ([JOSE](http://bit.ly/1KPQeWV)) are IETF RFCs.
 
 These articles ([1](http://bit.ly/Q6CfVR), [2](http://bit.ly/1BTWLLa)) describe the reasons above in greater detail.
 
#### Future Work

The application currently has the following security/authentication limitations that can be addressed in future work/merit further investigation:

* The JWT token is signed using HMAC, but not encrypted.
* The frontend application does not validate the received JWT token.
* The frontend application does not refresh the JWT token; so the user will start receiving 401 errors when the token 
expires. To work around this, the user can click on the `Sign out` button and then sign back in again.
* User registration is not supported.
* The API authentication endpoint currently allows CORS requests. Most third-party OAuth providers do not do so.
* The signing key used to sign JWT token requests is within the source code. I would look at moving it out, potentially
into a configuration management system.

### Authorization/RBAC

The API currently does not enforce any Role Based Access Control.


## Building the application locally

To run just the unit tests, type `mvn test`.

> One of the unit tests takes several seconds to run as it waits for a future to time out. This test has been tagged as being `slow`.

To run the unit and integration tests (which are identified by their names ending with `IntegrationTest`), run

    mvn verify

The integration tests use an in-memory H2 database.

## Miscellaneous Notes