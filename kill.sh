#!/usr/bin/env bash

SCRIPT_DIR=$(cd $(dirname $0) && pwd)
cd $SCRIPT_DIR

docker stop my_postgres
docker container rm my_postgres
docker image rm my_postgres

docker stop my_keycloak
docker container rm my_keycloak
docker image rm my_keycloak

docker compose -f superset/docker-compose-non-dev.yml down
