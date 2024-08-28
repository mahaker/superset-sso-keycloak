#!/usr/bin/env bash

SCRIPT_DIR=$(cd $(dirname $0) && pwd)
cd $SCRIPT_DIR

cd postgres
docker build -t my_postgres .
docker run --name my_postgres -p 5435:5432 --env-file ../env.list -d my_postgres
