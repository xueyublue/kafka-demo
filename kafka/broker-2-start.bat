pushd "%~dp0"

call .\bin\windows\kafka-server-start.bat .\config\server-2.properties

pause
