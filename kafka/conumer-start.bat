pushd "%~dp0"

call .\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic library-events

pause
