@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul 2>&1
echo ==========================================
echo   智慧云停车平台 - 数据库初始化脚本
echo ==========================================
echo.

set MYSQL_HOST=localhost
set MYSQL_PORT=3306
set MYSQL_USER=root
set MYSQL_PASS=123456
set DB_NAME=smart_parking_platform

echo 请输入MySQL连接参数（直接回车使用默认值）:
echo --------------------------------------------------

set /p INPUT_HOST="主机 [%MYSQL_HOST%]: "
if not "%INPUT_HOST%"=="" set MYSQL_HOST=%INPUT_HOST%

set /p INPUT_PORT="端口 [%MYSQL_PORT%]: "
if not "%INPUT_PORT%"=="" set MYSQL_PORT=%INPUT_PORT%

set /p INPUT_USER="用户名 [%MYSQL_USER%]: "
if not "%INPUT_USER%"=="" set MYSQL_USER=%INPUT_USER%

set /p INPUT_PASS="密码 [%MYSQL_PASS%]: "
if not "%INPUT_PASS%"=="" set MYSQL_PASS=%INPUT_PASS%

echo.
echo 使用的连接参数:
echo   主机: %MYSQL_HOST%
echo   端口: %MYSQL_PORT%
echo   用户: %MYSQL_USER%
echo   密码: %MYSQL_PASS%
echo.

set SCRIPT_DIR=%~dp0
set CREATE_TABLE_SQL=%SCRIPT_DIR%createtable.sql
set TEST_DATA_SQL=%SCRIPT_DIR%testdata.sql

echo.
echo [提示] 即将删除并重建数据库: %DB_NAME%
set /p CONFIRM="确认继续执行吗？(y/n): "
if /i not "%CONFIRM%"=="y" (
    echo 已取消操作
    pause
    exit /b 0
)

echo.
echo [0/3] 删除旧数据库（如存在）...
echo DROP DATABASE IF EXISTS %DB_NAME%; | mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% %DB_NAME% 2>nul
echo 数据库已清理
echo.

echo [1/3] 执行建表脚本
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% < "%CREATE_TABLE_SQL%"
if %errorlevel% neq 0 (
    echo 建表脚本执行失败！
    pause
    exit /b 1
)
echo 建表脚本执行成功！

for /f %%i in ('mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='%DB_NAME%';" 2^>nul') do set TABLE_COUNT=%%i
echo   - 数据库名: %DB_NAME%
echo   - 建表数量: !TABLE_COUNT! 张
echo.

echo [2/3] 执行测试数据脚本
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% < "%TEST_DATA_SQL%"
if %errorlevel% neq 0 (
    echo 测试数据脚本执行失败！
    pause
    exit /b 1
)
echo 测试数据脚本执行成功！

echo.
call :show_table_stats

echo [3/3] 数据库初始化完成！
echo ==========================================
echo   数据库初始化完成
echo   数据库名: %DB_NAME%
echo   建表数量: !TABLE_COUNT! 张
echo   插入数据: !TOTAL_COUNT! 条
echo ==========================================
pause
exit /b 0

:show_table_stats
echo   各表数据统计:
echo   ------------------------------------------------
for /f "tokens=1,2" %%a in ('mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% -N -e "SELECT table_name, table_rows FROM information_schema.tables WHERE table_schema='%DB_NAME%' ORDER BY table_name;" 2^>nul') do (
    echo   - %%a: %%b 条
)
echo   ------------------------------------------------
for /f %%i in ('mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% -N -e "SELECT SUM(table_rows) FROM information_schema.tables WHERE table_schema='%DB_NAME%';" 2^>nul') do set TOTAL_COUNT=%%i
echo   总计: !TOTAL_COUNT! 条
exit /b 0