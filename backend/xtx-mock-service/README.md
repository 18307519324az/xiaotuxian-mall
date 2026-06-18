# 小兔鲜儿 Mock Service

小兔鲜儿 Mock Service 是一个基于 Spring Boot 的本地接口服务，用于为小兔鲜儿 PC 商城前端提供演示数据。

## 技术栈

- Java 17
- Spring Boot 3
- Maven
- JSON 文件数据源

## 功能范围

- 首页数据
- 分类数据
- 商品数据
- 搜索数据
- 用户登录
- 购物车
- 地址管理
- 订单创建
- 支付模拟
- 订单查询
- 收藏与浏览历史
- 运行时数据重置

## 启动方式

```bash
mvn spring-boot:run
```

默认地址：

```
http://localhost:8099
```

## 常用接口

```
GET  /home/goods
GET  /home/category/head
POST /login
GET  /member/cart
POST /member/cart
POST /member/order
GET  /member/order
POST /admin/reset-member-data
```

## 关联项目

- 前端项目：`https://github.com/18307519324az/xiaotuxian-mall-frontend`
- 总项目：`https://github.com/18307519324az/xiaotuxian-mall`
