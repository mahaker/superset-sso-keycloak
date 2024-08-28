#!/usr/bin/env bash

SCRIPT_DIR=$(cd $(dirname $0) && pwd)
cd $SCRIPT_DIR

docker stop my_postgres
docker container rm my_postgres
docker image rm my_postgres
