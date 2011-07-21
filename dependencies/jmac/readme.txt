JMAC
=====================================================
version 1.74, June 30, 2006

Description
-----------
JMAC is a Java implementation of the Monkey's Audio decoder/JavaSound SPI.

To know more about Monkey's Audio please, visit this site:
http://www.monkeysaudio.com

It allows playing/compressing/converting APE, MAC and APL files up to version 3.99.
The main distributables is:

1. jmac.jar - full version (encoder and decoder).
2. jmacdecoder.jar - includes decoder only.
3. jmacplayer.jar - decoder + simple player.
4. jmacspi.jar - Java Sound SPI (1.3).
5. jmacspi15.jar - Java Sound SPI (1.5).
6. jmactritonusspi.jar - Tritonus (http://www.tritonus.org) based Java Sound
SPI. Thanks to Tritonus. This SPI should be used together with
tritonus_share.jar distributed in lib subdirectory.
7. jmac.dll - JNI wrapper around of original MAC library (Windows version).
8. libjmac.so - JNI wrapper around of original MAC library (linux version).

This is a non-commercial project and anyone can add his contribution.
JMAC is distributed under the terms of the FSF Gnu Public License (see gpl.txt).

Instructions
------------
Unzip to installation directory.

Required lib are distributed in the "distributables" directory. The required for Tritonus
based SPI (jmactritonusspi.jar) library "tritonus_share.jar" library are
distributed in the "lib" subdirectory.

Web Site: http://jmac.sourceforge.net/

FAQ : 
------------
- How to test the JMAC quickly?

  Please, look the bin directory for examples.

- How to use the JMAC?

  Please, look the davaguine.jmac.test package sources for working examples. Also,
  you can use jmac.jar as simple APE utility. Just enter the following command line:
  "java -jar jmac.jar" and read usage help to know how to use it.


- How to install JMAC SPI?
  Before running JMAC SPI you should set PATH and CLASSPATH for JAVA and you
  should add jmacspi.jar to the CLASSPATH. If you want to use Tritonus based SPI
  you should use jmactritonusspi.jar instead of jmacspi.jar and additionally add
  tritonus_share.jar to the CLASSPATH.

 
- How to enable the native mode of JMAC?

  You should set the jmac.NATIVE property to true value into system properties of JVM.
  Please, look the playNative.bat file for example. Also, you should be sure that the
  JVM can find the jmac.dll. So, this dll should be placed into work directory of
  application or the directory with this dll should be pointed in java.library.path
  system property.

- Do I need JMF to run JMAC player?
  No, JMF is not required. You just need a JVM JavaSound 1.0 compliant.
  (i.e. JVM1.3 or higher). However, JMAC is not JMF compliant.

- Does JMAC support streaming?
  Yes.

- How much CPU JMAC needs to run?
  Here are our benchmark notes:

  Insane - bigger than 100% under AMD Athlon 1700Mhz/Win2000+J2SE 1.4.2
  (Hotspot).

  Extra High compression - about 50% under AMD Athlon 1700Mhz/Win2000+J2SE 1.4.2
  (Hotspot).

  High compression - about 20% under AMD Athlon 1700Mhz/Win2000+J2SE 1.4.2 (Hotspot).

  Normal and Fast compression - less than 5% under AMD Athlon 1700Mhz/Win2000+J2SE
  1.4.2 (Hotspot).

  NOTE:
  In Native mode less than %25 for all types of compression under AMD Athlon
  1700Mhz/Win2000+J2SE 1.4.2 (Hotspot).

- How to contact JMAC developers?
  Please, visit project web page - http://jmac.sourceforge.net or you can email
  directly to davagin@mail.ru.
