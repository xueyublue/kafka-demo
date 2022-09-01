pushd "%~dp0"

call .\bin\windows\kafka-server-start.bat .\config\server-1.properties

pause
