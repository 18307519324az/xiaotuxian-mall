# 小兔鲜儿后端工程

本目录包含小兔鲜儿 B2C 电商平台的后端相关代码。默认演示流程使用 `xtx-mock-service` 提供本地接口数据，也保留了微服务模块用于进一步学习和扩展。

## 目录结构

```
backend/
├── xtx-mock-service/             # 本地 Mock Service
├── xtx-api/                      # 服务间 API 契约
├── xtx-common/                   # 公共能力模块
├── xtx-gateway/                  # 网关服务
├── xtx-services/                 # 业务微服务
├── deploy/                       # 本地基础设施配置
├── scripts/                      # 启动与维护脚本
├── sql/                          # 数据库脚本
└── pom.xml                       # Maven 父工程
```

## 核心模块

### xtx-mock-service

提供本地接口数据，默认用于前端联调和项目演示。

### xtx-api

按照业务领域拆分服务间接口契约，包括认证、商品、分类、购物车、订单、支付、会员等模块。

### xtx-common

提供通用能力封装，包括核心工具、Web 公共配置、安全配置、MyBatis-Flex 配置和 OpenAPI 配置。

### xtx-gateway

提供统一入口和路由能力。

### xtx-services

保留真实业务微服务模块，覆盖认证、会员、首页、分类、商品、库存、购物车、订单、支付、物流、评论等业务领域。

## 后端技术栈

- Java 17
- Spring Boot 3.2
- Spring Cloud 2023
- Spring Cloud Gateway
- Spring Cloud Alibaba / Nacos
- MyBatis-Flex 1.9
- MySQL 8.0
- Redis
- Druid
- Seata
- Sentinel
- Maven
- OpenAPI / Knife4j
- JWT
- Docker Compose

## 启动 Mock Service

在 `backend/` 目录下执行：

```bash
cd xtx-mock-service
mvn spring-boot:run
```

默认端口：

```
8099
```

## 微服务运行说明

`xtx-services/` 中的微服务模块需要配合 MySQL、Redis、Nacos 等基础服务使用。默认演示流程不要求启动完整微服务集群。
