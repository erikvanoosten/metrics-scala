#!/bin/bash
# Does a full cross build of all scala/akka versions.

DEFAULT_COMMAND="+publishSigned; sonatypeBundleRelease"

# Assumes osx
export JAVA_HOME=`/usr/libexec/java_home -v 11`

VERSION="$(git describe --tags)"
echo "Version: $VERSION"
echo "Java: $(java -version 2>&1 | head -n1)"

CMD="${1}"

if [ "$CMD" == "" ]; then
  CMD="$DEFAULT_COMMAND"
  if [[ "$VERSION" == *-* ]]; then
    echo "Only publish tagged versions!"
    exit 1
  fi
else
  CMD="+$CMD"
fi

sbt $CMD
