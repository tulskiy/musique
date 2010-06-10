#!/bin/sh
MUSIQUE_HOME=`dirname "$0"`
cd "$MUSIQUE_HOME"

# change this to use alternative JRE. If you are using 64-bit JRE,
# it is recommended to install ia32-sun-java6-jre to reduce memory
# and CPU usage, and set JAVA_PATH to eg. /usr/lib/jvm/ia32-java-6-sun/bin/java
export JAVA_PATH="java"

exec padsp $JAVA_PATH -client -Xms30m -Xmx60m -Djava.library.path=native:/usr/lib -jar musique.jar
