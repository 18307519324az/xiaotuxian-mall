# 数据库初始化脚本

本目录包含小兔鲜儿项目 4 个核心数据库的初始化脚本，用于首次搭建开发环境时快速创建数据库和表结构。

## 使用方法

按编号顺序执行：

```bash
mysql -u root -p < 01-init-xtx_category.sql
mysql -u root -p < 02-init-xtx_goods.sql
mysql -u root -p < 03-init-xtx_member.sql
mysql -u root -p < 04-init-xtx_auth.sql
```

或在 MySQL 客户端中执行：

```sql
source 01-init-xtx_category.sql;
source 02-init-xtx_goods.sql;
source 03-init-xtx_member.sql;
source 04-init-xtx_auth.sql;
```

## 与 Flyway 迁移的关系

各微服务使用 Flyway 管理数据库版本迁移，迁移脚本位于各服务的
`src/main/resources/db/migration/` 目录。

| 数据库 | 微服务 | Flyway 目录 |
|--------|--------|-------------|
| xtx_category | xtx-category-service | `services/xtx-category-service/.../db/migration/` |
| xtx_goods | xtx-goods-service | `services/xtx-goods-service/.../db/migration/` |
| xtx_member | xtx-member-service | `services/xtx-member-service/.../db/migration/` |
| xtx_auth | xtx-auth-service | `services/xtx-auth-service/.../db/migration/` |

**注意**：本目录的脚本仅用于数据库的首次初始化。
生产环境或持续开发中应使用 Flyway 迁移管理 schema 版本。

## v1.7 变更

- v1.7 新增本 `sql/init/` 目录，集中管理数据库初始化脚本
- 所有脚本仅在首次创建数据库时使用，不替代 Flyway 迁移
