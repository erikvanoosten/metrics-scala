#!/bin/bash
# Does a full cross build of all scala/akka versions.

DEFAULT_COMMAND=publish-signed

CMD=${1:-$DEFAULT_COMMAND}

if [ $CMD == $DEFAULT_COMMAND ]; then
  echo 'Did you tag already?'
  sleep 1
fi

sbt '; set akkaVersion := "";       set crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.0-RC2")' +$CMD \
    '; set akkaVersion := "2.2.5";  set crossScalaVersions := Seq("2.10.6")'           +$CMD \
    '; set akkaVersion := "2.3.15"; set crossScalaVersions := Seq("2.10.6", "2.11.8")' +$CMD \
    '; set akkaVersion := "2.4.11"; set crossScalaVersions := Seq("2.12.0-RC2")' +$CMD
