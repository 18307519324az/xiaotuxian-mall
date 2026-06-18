# 本地配置说明

本项目支持通过环境变量配置数据库密码、JWT 密钥和前端接口地址。默认演示流程使用本地 Mock Service，不需要连接完整微服务集群。

## 环境变量

| 变量名 | 说明 | 示例 |
|---|---|---|
| `DB_PASSWORD` | 本地数据库密码 | `your_local_dev_password` |
| `JWT_SECRET` | 本地 JWT 签名密钥 | `your_jwt_secret` |
| `VUE_APP_API_BASE_URL` | 前端接口地址 | `http://localhost:8099/` |

## Windows CMD 示例

```bat
set DB_PASSWORD=your_local_dev_password
set JWT_SECRET=your_jwt_secret
set VUE_APP_API_BASE_URL=http://localhost:8099/
```

## Windows PowerShell 示例

```powershell
$env:DB_PASSWORD="your_local_dev_password"
$env:JWT_SECRET="your_jwt_secret"
$env:VUE_APP_API_BASE_URL="http://localhost:8099/"
```

## 本地演示模式

本地演示模式默认使用 `backend/xtx-mock-service`。该服务使用本地 JSON 数据源提供接口数据，适合前端联调、功能演示和业务流程学习。

## 微服务模式

如需运行 `xtx-services/` 下的真实微服务模块，请先准备 MySQL、Redis、Nacos 等基础服务，并根据本地环境配置对应变量。
