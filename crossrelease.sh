#!/bin/bash
# Does a full cross build of all scala/akka versions.

DEFAULT_COMMAND=publish-signed

CMD=${1:-$DEFAULT_COMMAND}

VERSION="$(grep baseVersion build.sbt | head -n 1 | grep -o '".*"' | sed 's/"//g' | tr '[a-z]' '[A-Z]')"
echo "Version: $VERSION"

if [ $CMD == $DEFAULT_COMMAND ]; then
  if [[ $VERSION == *SNAPSHOT* ]]; then
    echo "Don't publish SNAPSHOTS!"
    exit 1
  fi
  echo 'Did you tag already?'
  sleep 1
fi

sbt '; set akkaVersion := "";       set crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.0")' +$CMD \
    '; set akkaVersion := "2.2.5";  set crossScalaVersions := Seq("2.10.6")'           +$CMD \
    '; set akkaVersion := "2.3.15"; set crossScalaVersions := Seq("2.10.6", "2.11.8")' +$CMD \
    '; set akkaVersion := "2.4.12"; set crossScalaVersions := Seq("2.12.0")' +$CMD
