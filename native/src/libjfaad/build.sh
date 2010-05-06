#!/bin/sh

gcc -I"/usr/lib/jvm/java-6-openjdk/include" -I"/usr/lib/jvm/java-6-openjdk/include/linux" -I. -O3 -c -D_JNI_IMPLEMENTATION_  -omain.out main.c;

gcc -s -Wall -D_JNI_IMPLEMENTATION_ -shared -o../../libjfaad.so main.out -L. -l mp4ff -l faad;

