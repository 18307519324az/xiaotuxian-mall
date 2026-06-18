#!/usr/bin/env bash
cd "$(dirname "$0")"
MAVEN_HOME="D:/apache-maven-3.9.11-bin/apache-maven-3.9.11"
"$JAVA_HOME/bin/java" -cp "$MAVEN_HOME/boot/plexus-classworlds-2.9.0.jar" \
  "-Dclassworlds.conf=$MAVEN_HOME/bin/m2.conf" \
  "-Dmaven.home=$MAVEN_HOME" \
  "-Dmaven.multiModuleProjectDirectory=$(pwd)" \
  org.codehaus.plexus.classworlds.launcher.Launcher \
  clean compile -DskipTests -q
