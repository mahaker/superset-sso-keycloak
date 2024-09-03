#!/usr/bin/env bash

SCRIPT_DIR=$(cd $(dirname $0) && pwd)
cd $SCRIPT_DIR

cd superset
docker compose -f docker-compose-non-dev.yml up -d
