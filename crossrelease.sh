#!/bin/bash
# Does a full cross build of all scala/akka versions.

DEFAULT_COMMAND=publish-signed

CMD=${1:-$DEFAULT_COMMAND}

sbt '; set akkaVersion := "";      set crossScalaVersions := Seq("2.10.5", "2.11.6")' +$CMD \
    '; set akkaVersion := "2.1.4"; set crossScalaVersions := Seq("2.10.5")'           +$CMD \
    '; set akkaVersion := "2.2.5"; set crossScalaVersions := Seq("2.10.5")'           +$CMD \
    '; set akkaVersion := "2.3.9"; set crossScalaVersions := Seq("2.10.5", "2.11.6")' +$CMD
