@echo off
IF EXIST build-conf\build-props.bat call build-conf\build-props.bat
SET ANT_HOME=.\thirdparty\ant
SET ANT_OPTS=%ANT_OPTS% -Dfile.encoding=UTF-8
call thirdparty\ant\bin\ant -f build.xml build-server-PROD
if ERRORLEVEL 1 pause