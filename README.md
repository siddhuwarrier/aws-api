[![Build Status](https://travis-ci.com/siddhuwarrier/aws-api.svg?token=psjzrsiRHzBi3DhiqeaA&branch=master)](https://travis-ci.com/siddhuwarrier/aws-api)

This is a simple Scala application written using the Scalatra framework that retrieves a list of instances off AWS using
the EC2 API.

## Source Code

The source code is in a private Bitbucket repository. Please contact [Siddhu Warrier](mailto:siddhu@siddhuw.info) if you
wish to access it.

    git clone git@github.com:siddhuwarrier/aws-api.git

## API Documentation

> NOTE: I would replace this with Swagger or something similar in production. An alternate option would be to use a GraphQL API such as https://edge.us.cdo.cisco.com/api-explorer)

| Endpoint        | Params | Response  |
| ------------- |:-------------:| -----:|
| POST /auth      | BODY {"username": username, "password": password} | 200 with JSON containing JWT token, 401 UNAUTHORIZED if username and/or password are invalid |
| GET /versions      |  | 200 with JSON containing git commit hash of latest commit |
| GET /api/aws/regions      | AUTHORIZATION header set with bearer token returned in  (Bearer `JWT token`)      |   200 with JSON of list of regions, 401 UNAUTHORIZED  |
| GET /api/aws/ec2/instances | AUTHORIZATION header set with bearer token returned in  (Bearer `JWT token`)     |    200 with JSON of list of instances, 401 UNAUTHORIZED, 500 INTERNAL SERVER ERROR (if AWS credentials invalid), 503 SERVICE UNAVAILABLE (if AWS inaccessible) |

## Build and deploy instructions

### Pre-requisites

- Java 11
- Maven 3.6
- Docker Engine (if you wish to run the service locally)
- AWS credentials (if you wish to run the service locally)

To run just tests, type `mvn test`. This will also run the integration tests (which use an in-memory H2 database).

To generate a code coverage report, run

    mvn scoverage:report -Pcoverage

The scoverage report is produced in `target/site/scoverage/index.html`. Due to a bug in the scoverage Maven plugin, none
of the hyperlinks from the main page work. However, you can open each individual HTML file for each class manually.

### Running locally

To run the app locally, first set your AWS access key and secret access key in a file in the root of your repo
called `.env` (in an AWS deployment, we would use IAM roles):

``` 
AWS_ACCESS_KEY_ID=<enter-your-access-key>
AWS_SECRET_ACCESS_KEY=<enter-your-secret-access-key>
```

run:

```
docker-compose build
docker-compose up -d
```

You can view the logs by typing:

```
docker logs -f <directory-name>_microservice_1
```

> Note: You may receive some warnings as Tomcat starts up.

The API should be accessible on http://localhost:8080. However, please note that you cannot access the API without first
authenticating.

The PostGres Docker container is initialised with a single user `admin` with the password `burak-crush-pineapple` (note:
the password is stored hashed and salted in the DB).

You can get a JWT token to make requests using this username and password as follows:

```
curl --silent http://localhost:8080/auth -X POST -d "{\"username\":\"admin\", \"password\":\"burak-crush-pineapple\"}" -H "Content-Type:application/json"
```

## Note on Logging

All logs are written in the `logstash` format. This would potentially allow for it to be fed into ElasticSearch and
viewed on Kibana.

## Future Work/Improvements

* Migration scripts for DB schema changes.
* API documentation using Swagger.
* Move hmac_key out of source control into configuration management.
* The OWSAP Enterprise Security API requires all logging go through log4j. Either switch to log4j or replace ESAPI.
