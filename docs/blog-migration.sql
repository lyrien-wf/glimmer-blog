-- ============================================
-- 博客数据库增量迁移脚本
-- ============================================

-- 分类表新增排序字段（2026-06-25）
ALTER TABLE `category` ADD COLUMN `sort_order` INT NOT NULL DEFAULT 0 AFTER `name`;

-- 文章搜索加速：FULLTEXT 索引（2026-06-25）
ALTER TABLE `article` ADD FULLTEXT INDEX `ft_search` (`title`, `content`);
