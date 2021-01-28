#!/usr/bin/env bash
dockerize -wait tcp://${DB_ENDPOINT}
java -jar target/dependency/webapp-runner.jar --port 8080 target/*.war
