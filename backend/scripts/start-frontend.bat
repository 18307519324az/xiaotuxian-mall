@echo off
REM ====================================
REM 启动前端开发服务器
REM ====================================
echo Starting Frontend on http://localhost:8080 ...
cd /d %~dp0..\vue3-rabbit-pc-master
call npm install
npm run serve
pause
