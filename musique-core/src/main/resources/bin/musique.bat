@echo off
setlocal enabledelayedexpansion

for /f "usebackq delims=" %%a in (musique.vmoptions) do (
    set vm_options=!vm_options! %%a
)

start javaw %vm_options% -jar ../${project.build.finalName}.jar