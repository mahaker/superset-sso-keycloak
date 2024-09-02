#!/usr/bin/env bash

SCRIPT_DIR=$(cd $(dirname $0) && pwd)
cd $SCRIPT_DIR

cd postgres
docker build -t my_postgres .
docker run --name my_postgres -p 5435:5432 --env-file ../.env -d my_postgres

cd ../keycloak
docker build -t my_keycloak .
# TODO production settings
# TODO enable /health endpoint(currently, 'Resource not found')
# memo: To connects my_postgres, set --add-host option
# https://stackoverflow.com/questions/24319662/from-inside-of-a-docker-container-how-do-i-connect-to-the-localhost-of-the-mach
docker run --name my_keycloak -p 8080:8080 -p 9000:9000 --env-file ../.env --add-host=host.docker.internal:host-gateway -d my_keycloak start-dev  --spi-theme-static-max-age=-1 --spi-theme-cache-themes=false --spi-theme-cache-templates=false

cd ../superset
docker compose -f docker-compose-non-dev.yml up -d
