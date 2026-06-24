-- ============================================
-- 博客数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE blog;

-- 用户表（仅管理员）
CREATE TABLE IF NOT EXISTS `user` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT,
  `username`   VARCHAR(50) NOT NULL UNIQUE,
  `password`   VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密',
  `created_at` DATETIME    NOT NULL DEFAULT NOW(),
  PRIMARY KEY (`id`)
);

-- 默认管理员（密码：admin123，部署后务必修改）
INSERT INTO `user` (username, password)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXEF0EJwOoNzKTSt9bVT.GEBFly')
ON DUPLICATE KEY UPDATE `username` = `username`;

-- 分类表
CREATE TABLE IF NOT EXISTS `category` (
  `id`   BIGINT      NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL UNIQUE,
  PRIMARY KEY (`id`)
);

-- 文章表
CREATE TABLE IF NOT EXISTS `article` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `title`        VARCHAR(200) NOT NULL,
  `summary`      VARCHAR(500)          COMMENT '摘要，为空时取正文前100字',
  `content`      LONGTEXT     NOT NULL  COMMENT 'Markdown 原文',
  `html_cache`   LONGTEXT              COMMENT '渲染后 HTML，加速读取',
  `cover_url`    VARCHAR(500)          COMMENT '封面图 URL',
  `category_id`  BIGINT                COMMENT '分类 ID',
  `is_published` TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '0草稿 1已发布',
  `views`        INT          NOT NULL DEFAULT 0,
  `created_at`   DATETIME     NOT NULL DEFAULT NOW(),
  `updated_at`   DATETIME     NOT NULL DEFAULT NOW() ON UPDATE NOW(),
  PRIMARY KEY (`id`),
  INDEX `idx_category` (`category_id`),
  INDEX `idx_published` (`is_published`),
  INDEX `idx_created` (`created_at`)
);

-- 标签表
CREATE TABLE IF NOT EXISTS `tag` (
  `id`   BIGINT      NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL UNIQUE,
  PRIMARY KEY (`id`)
);

-- 文章-标签关联表
CREATE TABLE IF NOT EXISTS `article_tag` (
  `article_id` BIGINT NOT NULL,
  `tag_id`     BIGINT NOT NULL,
  PRIMARY KEY (`article_id`, `tag_id`),
  INDEX `idx_tag` (`tag_id`)
);

-- 预置分类
INSERT INTO `category` (name) VALUES ('技术'), ('随笔'), ('教程'), ('生活')
ON DUPLICATE KEY UPDATE `name` = `name`;

-- 预置标签
INSERT INTO `tag` (name) VALUES ('Java'), ('Vue'), ('Spring Boot'), ('MySQL'), ('前端'), ('后端')
ON DUPLICATE KEY UPDATE `name` = `name`;
