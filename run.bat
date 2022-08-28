@echo off

cd /D %~dp0

start "MediaTextCreator" javaw -classpath "%~dp0\bin" -Xms16m -Xmx1024m com.asofterspace.mediaTextCreator.MediaTextCreator %*
