@echo off
REM ====================================
REM 启动 Mock 聚合服务
REM ====================================
echo Starting Mock Service on http://localhost:8099 ...
cd /d %~dp0..\xtx-mock-service
mvn spring-boot:run
pause
