#!/usr/bin/env bash
dockerize -wait tcp://db:5432
java -jar target/dependency/webapp-runner.jar --port 8080 target/*.war
