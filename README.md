# Glimmer Blog

基于 Spring Boot 3 + Vue 3 + MySQL 8 的个人博客系统，苹果风格简洁优雅设计。

## 快速开始

### 数据库初始化

1. 安装 MySQL 8.0
2. 执行初始化脚本：
```bash
mysql -u root -p < docs/blog-init.sql
```

### 后端启动

```bash
cd glimmer-blog-backend
# 修改 application.yml 中的数据库密码
mvn spring-boot:run
# 服务运行在 http://localhost:8080
```

### 前端启动

```bash
cd glimmer-blog-frontend
npm install
npm run dev
# 服务运行在 http://localhost:5173
```

### 默认账号

- 用户名：`admin`
- 密码：`admin123`
- ⚠️ 部署上线后请立即修改密码

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.2 + Spring Data JPA |
| 数据库 | MySQL 8.0 |
| 前端 | Vue 3 + Vite 5 |
| 编辑器 | Vditor |
| 渲染 | flexmark-java + highlight.js |
| 认证 | JWT (jjwt) |
| 部署 | 1Panel + Nginx |

## 部署（1Panel）

详见 `CLAUDE.md`。

核心步骤：
1. 1Panel 安装 MySQL 8
2. 后端打 JAR 包，通过 Supervisor 守护进程
3. 前端 `npm run build`，通过 1Panel 静态网站托管
4. Nginx 反向代理 `/api` 到后端 8080 端口
5. 申请 Let's Encrypt SSL 证书
