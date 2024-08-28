#!/usr/bin/env bash

docker stop my_postgres
docker container rm my_postgres
docker image rm my_postgres

docker stop my_keycloak
docker container rm my_keycloak
docker image rm my_keycloak
