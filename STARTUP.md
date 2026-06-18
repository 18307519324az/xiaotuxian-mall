# 本地启动说明

以下命令均从仓库根目录执行。

## 环境要求

| 工具 | 建议版本 |
|---|---|
| JDK | 17+ |
| Maven | 3.8+ |
| Node.js | 16+ |
| npm | 8+ |

## 启动 Mock Service

```bash
cd backend/xtx-mock-service
mvn spring-boot:run
```

默认地址：

```
http://localhost:8099
```

## 启动前端

```bash
cd frontend
npm install
npm run serve
```

默认地址：

```
http://localhost:8080
```

## 登录账号

```
账号：xiaotuxian001
密码：123456
```

## 常见问题

### 端口 8099 被占用

Windows 可使用以下命令查看占用进程：

```bat
netstat -ano | findstr 8099
```

结束进程：

```bat
taskkill /F /PID <PID>
```

### 前端页面无数据

请确认 Mock Service 已启动，并访问：

```
http://localhost:8099/home/goods
```

### 需要重置本地演示数据

```bash
curl -X POST http://localhost:8099/admin/reset-member-data
```
