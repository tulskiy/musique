#!/bin/sh
MUSIQUE_HOME=$(dirname "$0")
cd "$MUSIQUE_HOME"

# uncomment to use OSS emulation, fixes sound problems with Sun JRE's
#DSP="padsp"

# change this to use alternative JRE. If you are using 64-bit JRE,
# it is recommended to install ia32-sun-java6-jre to reduce memory
# and CPU usage, and set JAVA_PATH to eg. /usr/lib/jvm/ia32-java-6-sun/bin/java
if [ -z "$JAVA_PATH" ]; then
    JAVA_PATH="java"
fi
JVM_ARGS=$(tr '\n' ' ' < musique.vmoptions)
exec $DSP $JAVA_PATH $JVM_ARGS -jar ../${project.build.finalName}.jar
