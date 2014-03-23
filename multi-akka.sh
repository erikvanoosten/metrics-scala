#!/bin/bash

default_cmd=package

CMD=${1:-$default_cmd}

sbt "set akkaVersion := \"2.1.4\"" +$CMD "set akkaVersion := \"2.2.4\"" +$CMD "set akkaVersion := \"2.3.0\"" +$CMD
