@echo off
setlocal enabledelayedexpansion

for /f "usebackq delims=" %%a in (musique.vmoptions) do (
    set vm_options=!vm_options! %%a
)

set CLASSPATH=musique.jar
set CLASSPATH=%classpath%;lib/last.fm-bindings.jar
start javaw %vm_options% -cp %CLASSPATH% com.tulskiy.musique.system.Main