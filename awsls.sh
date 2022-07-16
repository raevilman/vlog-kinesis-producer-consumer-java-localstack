#!/bin/sh
exec aws --endpoint-url http://localhost:4566 --profile dummy "$@"