#!/bin/bash -e

set -x

if [ -z "$1" ]
  then
    echo "Missing parameters. Please enter the [Scala version]."
    echo "sbt-build-all.sh 2.13.3"
    exit 1
else
  : ${CURRENT_BRANCH_NAME:?"CURRENT_BRANCH_NAME is missing."}

  scala_version=$1
  echo "============================================"
  echo "Build projects"
  echo "--------------------------------------------"
  echo ""

  if [[ "$CURRENT_BRANCH_NAME" == "main" || "$CURRENT_BRANCH_NAME" == "release" ]]
  then
    sbt \
      -J-Xmx2048m \
      ++${scala_version}! \
      -v \
      clean \
      build
  else
    sbt \
      -J-Xmx2048m \
      ++${scala_version}! \
      -v \
      clean \
      devBuild
  fi


  echo "============================================"
  echo "Building projects: Done"
  echo "============================================"
fi
