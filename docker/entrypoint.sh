#!/bin/bash

set -e

echo "Running entrypoint.sh"

exec java -Xms256m -Xmx512m -jar $APP_DIRECTORY/$ARTIFACT