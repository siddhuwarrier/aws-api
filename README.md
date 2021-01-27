https://travis-ci.com/siddhuwarrier/aws-api.svg?token=psjzrsiRHzBi3DhiqeaA&branch=master

The Lockhart AWS API is a Scala application written using the Scalatra framework.

## Source Code

The source code is in a private Bitbucket repository. Please contact [Siddhu Warrier](mailto:siddhu@siddhuw.info) if you
wish to access it.

    git clone git@github.com:siddhuwarrier/aws-api.git
    
### Code Statistics

(Derived from scoverage report; see below on how to produce the report for yourself)
* Code Coverage: 90.29% (Statement), 100% (Branch)
* Number of tests: 45

## API Documentation

> NOTE: As mentioned below, I would replace this with Swagger or something similar in production. See http://swagger-ui.scansafe.cisco.com:8082 
(internal Cisco network) for an example of a Swagger UI my team deployed a few months ago.

| Endpoint        | Params | Response  |
| ------------- |:-------------:| -----:|
| POST /auth      | BODY {"username": username, "password": password} | 200 with JSON containing JWT token, 401 UNAUTHORIZED if username and/or password are invalid |
| GET /api/aws/regions      | AUTHORIZATION header set with bearer token returned in  (Bearer `JWT token`)      |   200 with JSON of list of regions, 401 UNAUTHORIZED  |
| GET /api/aws/ec2/instances | AUTHORIZATION header set with bearer token returned in  (Bearer `JWT token`)     |    200 with JSON of list of instances, 401 UNAUTHORIZED, 500 INTERNAL SERVER ERROR (if AWS credentials invalid), 503 SERVICE UNAVAILABLE (if AWS inaccessible) |


## Build and deploy instructions

To run just tests, type `mvn test`. This will also run the integration tests (which use an in-memory H2 database).

> The integration tests are not being run separately to the unit tests because of a limitation in the `scoverage` code
coverage tool I've used. The next version of `scoverage` should support this, as a result of which the pom.xml has been
left configured in order to run both the unit and the integration tests.

To generate a code coverage report, run
 
    mvn scoverage:report -Pcoverage
    
The scoverage report is produced in `target/site/scoverage/index.html`. Due to a bug in the scoverage Maven plugin, none of 
the hyperlinks from the main page work. However, you can open each individual HTML file for each class manually.
    
### Running locally

To run the app locally, first set up PostgreSQL, and create a database `aws_api`. Then, connect to the database as your user
and execute the following DDL statements.

    create table db_user(username varchar(256), pw_hash varchar(2048), salt varchar(2048));
    create unique index idx2541054d on db_user(username);
    
Then, edit `src/main/resources/app.conf` to set the following values (you do not need to change any of the other values):
  
      aws = {
        access_key_id = "ENTER_ACCESS_KEY_HERE"
        secret_access_key = "ENTER_SECRET_HERE"
      }
      
      db = {
        username = YOUR-POSTGRES-USERNAME
        password = YOUR-POSTGRES-PASSWORD
      }
  
Then, run

    mvn package
    java -jar target/dependency/webapp-runner.jar --port 8080 target/*.war
    
> Note: You may receive some warnings as Tomcat starts up.

The API should be accessible on http://localhost:8080. However, please note that you cannot access the API without first authenticating.
The easiest way of doing so is to start the frontend app up as described in the `frontend` directory/Git submodule. 
    
### Deployment to Heroku

To deploy the application to Heroku (you need a Heroku application set up, and your credentials configured in the Heroku
toolbelt), edit the below section of the `pom.xml` file and replace the `appName` with the name of your app 

            <configuration>
                <appName>YOUR APP NAME GOES HERE</appName>
                <processTypes>
                    <web>java $JAVA_OPTS -jar target/dependency/webapp-runner.jar --port $PORT target/*.war</web>
                </processTypes>
            </configuration>
            
Your Heroku application should contain the following add-ons:
* PostGreSQL

No migration scripts have been provided, as a result of which you will have to execute the following DDL after connecting
to the PostGres database using `heroku pg:psql` and execute the DDL statements specified in the previous section.

You will also need to use Heroku config vars for each of the settings in app.conf that you wish to override. `app.conf` has
the environment variables corresponding to each of those settings.

## Note on Logging

All logs are written in the `logstash` format. This would potentially allow for it to be fed into ElasticSearch and viewed
on Kibana.
    
## Future Work/Improvements

* Migration scripts for DB schema changes.
* API documentation using Swagger.
* Move hmac_key out of source control into configuration management.
* The OWSAP Enterprise Seucrity API requires all logging go through log4j. Either switch to log4j or replace ESAPI.
