gcc -I. -I"C:\Program Files (x86)\Java\jdk1.6.0_16\include" -I"C:\Program Files (x86)\Java\jdk1.6.0_16\include\win32" -I -O3 -Wall -c -fmessage-length=0 -Wall -D_JNI_IMPLEMENTATION_ -Wl,--kill-at -omain.o main.c

gcc -s -Wall -D_JNI_IMPLEMENTATION_ -Wl,--kill-at -shared -o..\..\libjfaad.dll main.o -L . -L ..\ -L ..\..\ -l mp4ff -l faad2