@echo off
IF EXIST build-conf\build-props.bat call build-conf\build-props.bat
SET ANT_HOME=.\thirdparty\ant
call thirdparty\ant\bin\ant -f build.xml
if ERRORLEVEL 1 pause