#!/bin/bash
# Does a full cross build of all scala/akka versions.

DEFAULT_COMMAND=publishSigned

CMD=${1:-$DEFAULT_COMMAND}

VERSION="$(git describe --tags)"
echo "Version: $VERSION"
echo "Java: $(java -version 2>&1 | head -n1)"

if [ $CMD == $DEFAULT_COMMAND ]; then
  if [[ $VERSION == *-* ]]; then
    echo "Only publish tagged versions!"
    exit 1
  fi
fi

sbt +$CMD
