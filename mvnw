#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# Apache Maven Wrapper startup script, version 3.3.2

if [ -z "$JAVA_HOME" ]; then
  JAVACMD=$(command -v java)
  if [ -z "$JAVACMD" ]; then
    echo "Error: JAVA_HOME is not set and java could not be found in PATH." >&2
    exit 1
  fi
else
  JAVACMD="$JAVA_HOME/bin/java"
fi

# Locate project base dir (the directory containing .mvn/)
MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-}"
if [ -z "$MAVEN_PROJECTBASEDIR" ]; then
  WDIR="$(pwd)"
  while true; do
    if [ -d "$WDIR/.mvn" ]; then
      MAVEN_PROJECTBASEDIR="$WDIR"
      break
    fi
    PARENT=$(dirname "$WDIR")
    if [ "$PARENT" = "$WDIR" ]; then
      MAVEN_PROJECTBASEDIR="$(pwd)"
      break
    fi
    WDIR="$PARENT"
  done
fi

WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_LAUNCHER="org.apache.maven.wrapper.MavenWrapperMain"

WRAPPER_URL=$(grep "^wrapperUrl" "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties" | cut -d'=' -f2)
WRAPPER_URL="${WRAPPER_URL:-https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar}"

# Download maven-wrapper.jar if missing
if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Downloading Maven wrapper..."
  if command -v curl > /dev/null 2>&1; then
    curl -fsSL -o "$WRAPPER_JAR" "$WRAPPER_URL"
  elif command -v wget > /dev/null 2>&1; then
    wget -q -O "$WRAPPER_JAR" "$WRAPPER_URL"
  else
    echo "Error: curl or wget is required to download the Maven wrapper." >&2
    exit 1
  fi
fi

exec "$JAVACMD" \
  $MAVEN_OPTS \
  -classpath "$WRAPPER_JAR" \
  "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" \
  "$WRAPPER_LAUNCHER" "$@"
