@echo off
cd /d %~dp0
java -jar smart-parking-cloud-platform-0.0.1-SNAPSHOT.jar --spring.config.location=./application-dev.yml --logging.config=./logback-spring.xml
pause