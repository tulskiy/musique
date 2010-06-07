#!/bin/sh
MUSIQUE_HOME=`dirname "$0"`
cd "$MUSIQUE_HOME"
exec java -client -Xms40m -Xmx60m -Djava.library.path=native -jar musique.jar
