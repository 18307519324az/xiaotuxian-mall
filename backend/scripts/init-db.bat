@echo off
REM ============================================
REM 小兔鲜儿数据库初始化脚本 (Windows)
REM 依次执行所有SQL文件初始化数据库
REM ============================================

set MYSQL_USER=root
set MYSQL_PASS=root
set SQL_DIR=..\sql

echo ============================================
echo 开始初始化小兔鲜儿数据库...
echo ============================================

echo [1/10] 初始化认证数据库 (xtx_auth)...
mysql -u %MYSQL_USER% -p%MYSQL_PASS% < %SQL_DIR%\xtx_auth\V1__init.sql
if %errorlevel% neq 0 (echo 初始化 xtx_auth 失败! & exit /b %errorlevel%)
echo 完成.

echo [2/10] 初始化内容管理数据库 (xtx_cms)...
mysql -u %MYSQL_USER% -p%MYSQL_PASS% < %SQL_DIR%\xtx_cms\V1__init.sql
if %errorlevel% neq 0 (echo 初始化 xtx_cms 失败! & exit /b %errorlevel%)
echo 完成.

echo [3/10] 初始化分类数据库 (xtx_category)...
mysql -u %MYSQL_USER% -p%MYSQL_PASS% < %SQL_DIR%\xtx_category\V1__init.sql
if %errorlevel% neq 0 (echo 初始化 xtx_category 失败! & exit /b %errorlevel%)
echo 完成.

echo [4/10] 初始化商品数据库 (xtx_goods)...
mysql -u %MYSQL_USER% -p%MYSQL_PASS% < %SQL_DIR%\xtx_goods\V1__init.sql
if %errorlevel% neq 0 (echo 初始化 xtx_goods 失败! & exit /b %errorlevel%)
echo 完成.

echo [5/10] 初始化库存数据库 (xtx_inventory)...
mysql -u %MYSQL_USER% -p%MYSQL_PASS% < %SQL_DIR%\xtx_inventory\V1__init.sql
if %errorlevel% neq 0 (echo 初始化 xtx_inventory 失败! & exit /b %errorlevel%)
echo 完成.

echo [6/10] 初始化购物车数据库 (xtx_cart)...
mysql -u %MYSQL_USER% -p%MYSQL_PASS% < %SQL_DIR%\xtx_cart\V1__init.sql
if %errorlevel% neq 0 (echo 初始化 xtx_cart 失败! & exit /b %errorlevel%)
echo 完成.

echo [7/10] 初始化订单数据库 (xtx_order)...
mysql -u %MYSQL_USER% -p%MYSQL_PASS% < %SQL_DIR%\xtx_order\V1__init.sql
if %errorlevel% neq 0 (echo 初始化 xtx_order 失败! & exit /b %errorlevel%)
echo 完成.

echo [8/10] 初始化支付数据库 (xtx_payment)...
mysql -u %MYSQL_USER% -p%MYSQL_PASS% < %SQL_DIR%\xtx_payment\V1__init.sql
if %errorlevel% neq 0 (echo 初始化 xtx_payment 失败! & exit /b %errorlevel%)
echo 完成.

echo [9/10] 初始化会员数据库 (xtx_member)...
mysql -u %MYSQL_USER% -p%MYSQL_PASS% < %SQL_DIR%\xtx_member\V1__init.sql
if %errorlevel% neq 0 (echo 初始化 xtx_member 失败! & exit /b %errorlevel%)
echo 完成.

echo [10/10] 初始化评价数据库 (xtx_comment)...
mysql -u %MYSQL_USER% -p%MYSQL_PASS% < %SQL_DIR%\xtx_comment\V1__init.sql
if %errorlevel% neq 0 (echo 初始化 xtx_comment 失败! & exit /b %errorlevel%)
echo 完成.

echo ============================================
echo 所有数据库初始化完成!
echo ============================================
pause
