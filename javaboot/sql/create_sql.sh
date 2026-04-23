#!/bin/bash

echo "=========================================="
echo "  智慧云停车平台 - 数据库初始化脚本"
echo "=========================================="
echo ""

MYSQL_HOST="${MYSQL_HOST:-localhost}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASS="${MYSQL_PASS:-123456}"
DB_NAME="smart_parking"

echo "请输入MySQL连接参数（直接回车使用默认值）:"
echo "--------------------------------------------------"

read -p "主机 [$MYSQL_HOST]: " INPUT_HOST
[ -n "$INPUT_HOST" ] && MYSQL_HOST="$INPUT_HOST"

read -p "端口 [$MYSQL_PORT]: " INPUT_PORT
[ -n "$INPUT_PORT" ] && MYSQL_PORT="$INPUT_PORT"

read -p "用户名 [$MYSQL_USER]: " INPUT_USER
[ -n "$INPUT_USER" ] && MYSQL_USER="$INPUT_USER"

read -p "密码 [$MYSQL_PASS]: " INPUT_PASS
[ -n "$INPUT_PASS" ] && MYSQL_PASS="$INPUT_PASS"

echo ""
echo "使用的连接参数:"
echo "  主机: $MYSQL_HOST"
echo "  端口: $MYSQL_PORT"
echo "  用户: $MYSQL_USER"
echo "  密码: $MYSQL_PASS"
echo ""

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
CREATE_TABLE_SQL="$SCRIPT_DIR/create_table.sql"
TEST_DATA_SQL="$SCRIPT_DIR/test_data.sql"

echo ""
echo "[提示] 即将删除并重建数据库: $DB_NAME"
read -p "确认继续执行吗？(y/n): " CONFIRM
if [ "$CONFIRM" != "y" ] && [ "$CONFIRM" != "Y" ]; then
    echo "已取消操作"
    exit 0
fi

echo ""
echo "[0/3] 删除旧数据库（如存在）..."
mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASS" -e "DROP DATABASE IF EXISTS $DB_NAME;" 2>/dev/null
echo "数据库已清理"
echo ""

echo "[1/3] 执行建表脚本: $CREATE_TABLE_SQL"
mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASS" < "$CREATE_TABLE_SQL"
if [ $? -ne 0 ]; then
    echo "建表脚本执行失败！"
    exit 1
fi
echo "建表脚本执行成功！"

TABLE_COUNT=$(mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASS" -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$DB_NAME';")
echo "  - 数据库名: $DB_NAME"
echo "  - 建表数量: $TABLE_COUNT 张"
echo ""

echo "[2/3] 执行测试数据脚本: $TEST_DATA_SQL"
mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASS" < "$TEST_DATA_SQL"
if [ $? -ne 0 ]; then
    echo "测试数据脚本执行失败！"
    exit 1
fi
echo "测试数据脚本执行成功！"

echo ""
echo "  各表数据统计:"
echo "  ------------------------------------------------"
mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASS" -N -e "SELECT CONCAT('  - ', table_name, ': ', table_rows, ' 条') FROM information_schema.tables WHERE table_schema='$DB_NAME' ORDER BY table_name;" 2>/dev/null
echo "  ------------------------------------------------"
TOTAL_COUNT=$(mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASS" -N -e "SELECT SUM(table_rows) FROM information_schema.tables WHERE table_schema='$DB_NAME';")
echo "  总计: $TOTAL_COUNT 条"
echo ""

echo "[3/3] 数据库初始化完成！"
echo "=========================================="
echo "  数据库初始化完成！"
echo "  数据库名: $DB_NAME"
echo "  建表数量: $TABLE_COUNT 张"
echo "  插入数据: $TOTAL_COUNT 条"
echo "=========================================="