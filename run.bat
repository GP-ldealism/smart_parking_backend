@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul 2>&1
echo ==========================================
echo   智慧云停车平台 - 启动脚本
echo ==========================================
echo.

set JAR_NAME=smart-parking-cloud-platform.jar
set CONFIG_FILE=application-dev.yml
set LOGBACK_CONFIG=logback-spring.xml
set LOG_DIR=%~dp0log

echo 请输入JAR包名称（直接回车使用默认: %JAR_NAME%）:
set /p INPUT_JAR="JAR包名称 [%JAR_NAME%]: "
if not "%INPUT_JAR%"=="" set JAR_NAME=%INPUT_JAR%

echo 请输入Spring配置文件（直接回车使用默认: %CONFIG_FILE%）:
set /p INPUT_CONFIG="配置文件 [%CONFIG_FILE%]: "
if not "%INPUT_CONFIG%"=="" set CONFIG_FILE=%INPUT_CONFIG%

echo 请输入Logback配置文件（直接回车使用默认: %LOGBACK_CONFIG%）:
set /p INPUT_LOGBACK="Logback配置 [%LOGBACK_CONFIG%]: "
if not "%INPUT_LOGBACK%"=="" set LOGBACK_CONFIG=%INPUT_LOGBACK%

if not exist "%JAR_NAME%" (
    echo [错误] JAR包不存在: %JAR_NAME%
    echo 请确认JAR包与本脚本在同一目录下
    pause
    exit /b 1
)

if not exist "%CONFIG_FILE%" (
    echo [错误] Spring配置文件不存在: %CONFIG_FILE%
    echo 请确认配置文件与本脚本在同一目录下
    pause
    exit /b 1
)

if not exist "%LOGBACK_CONFIG%" (
    echo [警告] Logback配置文件不存在: %LOGBACK_CONFIG%
    echo 将使用JAR包内的默认配置
    set LOGBACK_PARAM=
) else (
    set LOGBACK_PARAM=--logging.config=./%LOGBACK_CONFIG%
)

if not exist "%LOG_DIR%" (
    mkdir "%LOG_DIR%" 2>nul
    echo [提示] 已创建日志目录: %LOG_DIR%
)

for /f "tokens=2 delims==" %%a in ('wmic os get localdatetime /value 2^>nul') do set "DT=%%a"
set LOG_FILE=%LOG_DIR%\%DT:~0,4%_%DT:~4,2%_%DT:~6,2%.log

echo.
echo 启动参数:
echo   JAR包: %JAR_NAME%
echo   Spring配置: %CONFIG_FILE%
echo   Logback配置: %LOGBACK_CONFIG%
echo   日志目录: %LOG_DIR%
echo   今日日志: %LOG_FILE%
echo.
echo 正在启动服务...
echo 按 Ctrl+C 可以停止服务
echo.

java -jar "%JAR_NAME%" --spring.config.location=./%CONFIG_FILE% %LOGBACK_PARAM% > "%LOG_FILE%" 2>&1

echo.
echo 服务已退出，查看日志: %LOG_FILE%
pause