-- 创建数据库
create database if not exists `simior-blog` default character set utf8mb4 collate utf8mb4_unicode_ci;
use `simior-blog`;

-- =============================================
-- 用户与权限相关表
-- =============================================

-- 角色表
drop table if exists `sys_role`;
create table `sys_role`
(
    `id`          bigint(20)  not null auto_increment comment '角色ID',
    `role_name`   varchar(50) not null comment '角色名称',
    `role_key`    varchar(50) not null comment '角色标识',
    `role_sort`   int(11)    default '0' COMMENT '显示顺序',
    `status`      tinyint(1) default '1' COMMENT '状态 0-禁用 1-启用',
    `deleted`     tinyint(1) default '0' COMMENT '逻辑删除 0-未删除 1-已删除',
    `create_time` datetime   default current_timestamp comment '创建时间',
    `update_time` datetime   default current_timestamp on update current_timestamp comment '更新时间',
    primary key (`id`),
    unique KEY `uk_role_key` (`role_key`)
) engine = InnoDB
  default charset = utf8mb4 comment '角色表';
-- 插入角色数据
insert into `sys_role` (`role_name`, `role_key`, `role_sort`, `status`)
values ('管理员', 'admin', 1, 1),
       ('作者', 'author', 2, 1),
       ('普通用户', 'user', 3, 1);


-- 用户表 (User)
drop table if exists `sys_user`;
create table `sys_user`
(
    `id`          bigint(20)   not null auto_increment comment '用户id',
    `username`    varchar(50)  not null comment '用户名',
    `password`    varchar(200) not null comment '密码(BCrypt哈希值)',
    `nickname`    varchar(50)  default null comment '昵称',
    `email`       varchar(100) default null comment '邮箱',
    `phone`       varchar(20)  default null comment '手机号',
    `avatar`      varchar(500) default null comment '头像',
    `intro`       varchar(500) default null comment '个人简介',
    `role_id`     bigint(20)   default null comment '角色id',
    `status`      tinyint(1)   default '1' comment '状态 0-禁用 1-启用',
    `deleted`     tinyint(1)   default '0' comment '逻辑删除 0-未删除 1-已删除',
    `create_time` datetime     default current_timestamp comment '创建时间',
    `update_time` datetime     default current_timestamp on update current_timestamp comment '更新时间',
    primary key (`id`),
    unique KEY `uk_username` (`username`),
    unique KEY `uk_email` (`email`),
    unique KEY `uk_phone` (`phone`),
    key `idx_role_id` (`role_id`)
) engine = InnoDB
  default charset = utf8mb4 comment '用户表';
-- 插入用户
insert into `sys_user` (`username`, `password`, `nickname`, `email`, `phone`, `avatar`, `intro`, `role_id`, `status`)
values ('admin', '$2a$10$cHj8oPvn9Uxu4SKdvudTIekwS60aHcQcQ5vql/3Oa0ON2QpnI75gK', '管理员', 'admin@example.com',
        '13800138000',
        'https://gitee.com/simior/simior-blog/raw/master/avatar.jpg', '这是管理员的个人简介', 1, 1),
       ('author', '$2a$10$cHj8oPvn9Uxu4SKdvudTIekwS60aHcQcQ5vql/3Oa0ON2QpnI75gK', '小石', 'author@example.com',
        '13800138001',
        'https://gitee.com/simior/simior-blog/raw/master/avatar.jpg', '热爱写作的程序员', 2, 1),
       ('user', '$2a$10$cHj8oPvn9Uxu4SKdvudTIekwS60aHcQcQ5vql/3Oa0ON2QpnI75gK', '普通用户', 'user@example.com',
        '13800138002',
        'https://gitee.com/simior/simior-blog/raw/master/avatar.jpg', '喜欢阅读技术文章', 3, 1);

-- 用户角色关联表（已废弃：当前使用 sys_user.role_id 字段直接关联角色，此表不再使用）
drop table if exists `sys_user_role`;
create table `sys_user_role`
(
    `id`          bigint(20) not null auto_increment comment 'ID',
    `user_id`     bigint(20) not null comment '用户ID',
    `role_id`     bigint(20) not null comment '角色ID',
    `create_time` datetime default current_timestamp comment '创建时间',
    primary key (`id`),
    key `idx_user_id` (`user_id`),
    key `idx_role_id` (`role_id`)
) engine = InnoDB
  default charset = utf8mb4 comment '用户角色关联表';
-- 插入用户角色关联数据
insert into `sys_user_role` (`user_id`, `role_id`)
values (1, 1),
       (2, 2),
       (3, 3);

-- =============================================
-- 文章相关表
-- =============================================

-- 分类表
drop table if exists `blog_category`;
create table `blog_category`
(
    `id`            bigint(20)  not null auto_increment comment '分类id',
    `category_name` varchar(50) not null comment '分类名称',
    `category_desc` varchar(200) default null comment '分类描述',
    `sort`          int(11)      default '0' comment '排序',
    `deleted`       tinyint(1)   default '0' comment '逻辑删除 0-未删除 1-已删除',
    `create_time`   datetime     default current_timestamp comment '创建时间',
    `update_time`   datetime     default current_timestamp on update current_timestamp comment '更新时间',
    primary key (`id`)
) engine = InnoDB
  default charset = utf8mb4 comment '分类表';
-- 插入分类数据
insert into `blog_category` (`category_name`, `category_desc`, `sort`)
values ('Java', 'Java技术相关文章', 1),
       ('Spring', 'Spring框架相关文章', 2),
       ('前端', '前端技术相关文章', 3),
       ('数据库', '数据库相关文章', 4),
       ('算法', '算法与数据结构', 5),
       ('架构', '系统架构与设计', 6);

-- 标签表
drop table if exists `blog_tag`;
create table `blog_tag`
(
    `id`          bigint(20) auto_increment comment '标签id',
    `tag_name`    varchar(25) not null unique comment '标签名称',
    `deleted`     tinyint(1) default '0' comment '逻辑删除 0-未删除 1-已删除',
    `create_time` datetime   default current_timestamp comment '创建时间',
    `update_time` datetime   default current_timestamp on update current_timestamp comment '更新时间',
    primary key (`id`)
) engine = InnoDB
  default charset = utf8mb4 comment '标签表';
-- 插入标签数据
insert into `blog_tag` (`tag_name`)
values ('Spring Boot'),
       ('MyBatis'),
       ('Vue3'),
       ('MySQL'),
       ('Redis'),
       ('微服务'),
       ('分布式'),
       ('高并发'),
       ('设计模式'),
       ('算法');

-- 文章表
drop table if exists `blog_article`;
create table `blog_article`
(
    `id`            bigint(20)   not null auto_increment comment '文章id',
    `user_id`       bigint(20)   not null comment '作者id',
    `category_id`   bigint(20)   default null comment '分类id',
    `title`         varchar(200) not null comment '文章标题',
    `summary`       varchar(500) default null comment '文章摘要',
    `cover_image`   varchar(500) default null comment '封面图',
    `content`       longtext     not null comment '文章内容(Markdown)',
    `html_content`  longtext     default null comment 'HTML内容',
    `is_top`        tinyint(1)   default '0' comment '是否置顶 0-否 1-是',
    `is_draft`      tinyint(1)   default '0' comment '是否草稿 0-否 1-是',
    `audit_status`  tinyint(1)   default '1' comment '审核状态 0-待审核 1-审核通过 2-审核拒绝',
    `view_count`    int(11)      default '0' comment '浏览量',
    `like_count`    int(11)      default '0' comment '点赞数',
    `comment_count` int(11)      default '0' comment '评论数',
    `collect_count` int(11)      default '0' comment '收藏数',
    `version`       int(11)      default '1' comment '文章版本号',
    `deleted`       tinyint(1)   default '0' comment '逻辑删除 0-未删除 1-已删除',
    `create_time`   datetime     default current_timestamp comment '创建时间',
    `update_time`   datetime     default current_timestamp on update current_timestamp comment '更新时间',
    primary key (`id`),
    key `idx_user_id` (`user_id`),
    key `idx_category_id` (`category_id`),
    key `idx_is_top` (`is_top`),
    key `idx_create_time` (`create_time`)
) engine = InnoDB
  default charset = utf8mb4 comment '文章表';
-- 插入文章示例数据
insert into `blog_article` (`user_id`, `category_id`, `title`, `summary`, `cover_image`, `content`, `is_top`,
                            `is_draft`, `view_count`, `like_count`, `comment_count`)
values (2, 1, 'Spring Boot 4.0 入门教程', 'Spring Boot 4.0是Spring框架的最新版本,本文将带你快速入门',
        'https://gitee.com/simior/simior-blog/raw/master/article/home-banner.png',
        '## 😲 md-editor-v3

Markdown Editor for Vue3, developed in jsx and typescript, support different themes、beautify content by prettier.

### 🤖 Base

**bold**, <u>underline</u>, _italic_, ~~line-through~~, superscript^26^, subscript~1~, `inline code`, [link](https://github.com/imzbf)

> quote: I Have a Dream

1. So even though we face the difficulties of today and tomorrow, I still have a dream.
2. It is a dream deeply rooted in the American dream.
3. I have a dream that one day this nation will rise up.

- [ ] Friday
- [ ] Saturday
- [x] Sunday

![Picture](https://imzbf.github.io/md-editor-rt/imgs/mark_emoji.gif)

## 🤗 Code

```vue
<template>
  <MdEditor v-model="text" />
</template>

<script setup>
import { ref } from ''vue'';
import { MdEditor } from ''md-editor-v3'';
import ''md-editor-v3/lib/style.css'';

const text = ref(''Hello Editor!'');
</script>
```

## 🖨 Text

The Old Man and the Sea served to reinvigorate Hemingway''s literary reputation and prompted a reexamination of his entire body of work.

## 📈 Table

| THead1          |      THead2       |           THead3 |
| :-------------- | :---------------: | ---------------: |
| text-align:left | text-align:center | text-align:right |

## 📏 Formula

Inline: $x+y^{2x}$

$$
\sqrt[3]{x}
$$

## 🧬 Diagram

mermaid

```mermaid
flowchart TD
  Start --> Stop
```

echarts

```echarts
{
  tooltip: {
    trigger: ''axis''
  },
  xAxis: {
    type: ''category'',
    data: [''Mon'', ''Tue'', ''Wed'', ''Thu'', ''Fri'', ''Sat'', ''Sun'']
  },
  yAxis: {
    type: ''value''
  },
  series: [
    {
      data: [150, 230, 224, 218, 135, 147, 260],
      type: ''line''
    }
  ]
}
```

## 🪄 Alert

!!! note Supported Types

note、abstract、info、tip、success、question、warning、failure、danger、bug、example、quote、hint、caution、error、attention

!!!

## ☘️ em...

none
',
        1, 0, 156, 23, 5),
       (2, 3, 'Vue3 组合式API完全指南', 'Vue3带来了全新的组合式API,让代码更加优雅和可维护',
        'https://gitee.com/simior/simior-blog/raw/master/article/bg.png',
        '# Vue3 组合式API完全指南\n\n## 什么是组合式API\n\n组合式API是Vue3的核心特性...\n\n## 基本使用\n\n```js\nsetup() {\n  // your code\n}\n```',
        0, 0, 234, 45, 12),
       (1, 2, 'MyBatis-Plus实战教程', 'MyBatis-Plus为简化开发而生,让你的CRUD操作更加简单',
        'https://gitee.com/simior/simior-blog/raw/master/article/home-banner.png',
        '# MyBatis-Plus实战教程\n\n## 简介\n\nMyBatis-Plus是一个MyBatis的增强工具...', 0, 0, 189, 34, 8);

-- 文章标签关联表
drop table if exists `blog_article_tag`;
create table `blog_article_tag`
(
    `id`          bigint(20) not null auto_increment comment 'id',
    `article_id`  bigint(20) not null comment '文章id',
    `tag_id`      bigint(20) not null comment '标签id',
    `create_time` datetime default current_timestamp comment '创建时间',
    primary key (`id`),
    key `idx_article_id` (`article_id`),
    key `idx_tag_id` (`tag_id`)
) engine = InnoDB
  default charset = utf8mb4 comment '文章标签关联表';
-- 插入文章标签关联数据
insert into `blog_article_tag` (`article_id`, `tag_id`)
values (1, 1),
       (1, 2),
       (2, 3),
       (3, 2),
       (3, 4);


-- =============================================
-- 评论互动相关表
-- =============================================

-- 评论表
drop table if exists `blog_comment`;
create table `blog_comment`
(
    `id`            bigint(20) not null auto_increment comment '评论id',
    `article_id`    bigint(20) not null comment '文章id',
    `user_id`       bigint(20) not null comment '评论用户id',
    `parent_id`     bigint(20) default null comment '父评论id(二级回复)',
    `reply_user_id` bigint(20) default null comment '回复用户id',
    `content`       text       not null comment '评论内容',
    `like_count`    int(11)    default '0' comment '点赞数',
    `audit_status`  tinyint(1) default '1' comment '审核状态 0-待审核 1-审核通过 2-审核拒绝',
    `deleted`       tinyint(1) default '0' comment '逻辑删除 0-未删除 1-已删除',
    `create_time`   datetime   default current_timestamp comment '创建时间',
    `update_time`   datetime   default current_timestamp on update current_timestamp comment '更新时间',
    primary key (`id`),
    key `idx_article_id` (`article_id`),
    key `idx_user_id` (`user_id`),
    key `idx_parent_id` (`parent_id`)
) engine = InnoDB
  default charset = utf8mb4 comment '评论表';
-- 插入评论示例数据
insert into `blog_comment` (`article_id`, `user_id`, `content`, `like_count`)
values (1, 2, '写得真好,学习了!', 5),
       (1, 1, '感谢分享,很有帮助', 3),
       (2, 2, 'Vue3确实很强大', 8);
-- 插入二级评论
insert into `blog_comment` (`article_id`, `user_id`, `parent_id`, `reply_user_id`, `content`, `like_count`)
values (1, 2, 1, 2, '谢谢支持!', 2);


-- 评论点赞表
drop table if exists `blog_comment_like`;
create table `blog_comment_like`
(
    `id`          bigint(20) not null auto_increment comment 'ID',
    `comment_id`  bigint(20) not null comment '评论ID',
    `user_id`     bigint(20) not null comment '用户ID',
    `create_time` datetime default current_timestamp comment '创建时间',
    primary key (`id`),
    unique key `uk_comment_user` (`comment_id`, `user_id`),
    key `idx_user_id` (`user_id`)
) engine = InnoDB
  default charset = utf8mb4 comment '评论点赞表';

-- =============================================
-- 用户互动相关表
-- =============================================

-- 文章点赞表
drop table if exists `blog_article_like`;
create table `blog_article_like`
(
    `id`          bigint(20) not null auto_increment comment 'ID',
    `article_id`  bigint(20) not null comment '文章ID',
    `user_id`     bigint(20) not null comment '用户ID',
    `create_time` datetime default current_timestamp comment '创建时间',
    primary key (`id`),
    unique key `uk_article_user` (`article_id`, `user_id`),
    key `idx_user_id` (`user_id`)
) engine = InnoDB
  default charset = utf8mb4 comment '文章点赞表';

-- 文章收藏表
drop table if exists `blog_article_collect`;
create table `blog_article_collect`
(
    `id`          bigint(20) not null auto_increment comment 'ID',
    `article_id`  bigint(20) not null comment '文章ID',
    `user_id`     bigint(20) not null comment '用户ID',
    `create_time` datetime default current_timestamp comment '创建时间',
    primary key (`id`),
    unique key `uk_article_user` (`article_id`, `user_id`),
    key `idx_user_id` (`user_id`)
) engine = InnoDB
  default charset = utf8mb4 comment '文章收藏表';

-- 浏览记录表
drop table if exists `blog_view_history`;
create table `blog_view_history`
(
    `id`          bigint(20) not null auto_increment comment 'ID',
    `article_id`  bigint(20) not null comment '文章ID',
    `user_id`     bigint(20)  default null comment '用户ID(未登录则为NULL)',
    `ip_address`  varchar(50) default null comment 'IP地址',
    `create_time` datetime    default current_timestamp comment '创建时间',
    primary key (`id`),
    key `idx_article_id` (`article_id`),
    key `idx_user_id` (`user_id`)
) engine = InnoDB
  default charset = utf8mb4 comment '浏览记录表';

-- =============================================
-- 消息通知相关表
-- =============================================

-- 消息通知表
drop table if exists `blog_message`;
create table `blog_message`
(
    `id`           bigint(20) not null auto_increment comment '消息ID',
    `user_id`      bigint(20) not null comment '接收用户ID',
    `from_user_id` bigint(20)   default null COMMENT '发送用户ID',
    `type`         tinyint(1) not null comment '消息类型 1-评论 2-点赞 3-收藏 4-系统通知',
    `content`      varchar(500) default null COMMENT '消息内容',
    `article_id`   bigint(20)   default null COMMENT '相关文章ID',
    `comment_id`   bigint(20)   default null COMMENT '相关评论ID',
    `is_read`      tinyint(1)   default '0' COMMENT '是否已读 0-未读 1-已读',
    `create_time`  datetime     default current_timestamp comment '创建时间',
    primary key (`id`),
    key `idx_user_id` (`user_id`),
    key `idx_from_user_id` (`from_user_id`)
) engine = InnoDB
  default charset = utf8mb4 comment '消息通知表';

-- =============================================
-- 首页展示相关表
-- =============================================

-- 轮播图表
drop table if exists `blog_banner`;
create table `blog_banner`
(
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '轮播图ID',
    `title`       varchar(100) DEFAULT NULL COMMENT '标题',
    `image_url`   varchar(500) NOT NULL COMMENT '图片URL',
    `link_url`    varchar(500) DEFAULT NULL COMMENT '链接URL',
    `sort`        int(11)      DEFAULT '0' COMMENT '排序',
    `status`      tinyint(1)   DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
    `deleted`     tinyint(1)   DEFAULT '0' COMMENT '逻辑删除 0-未删除 1-已删除',
    `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='轮播图表';
-- 插入轮播图数据
insert into `blog_banner` (`title`, `image_url`, `link_url`, `sort`, `status`)
values ('欢迎来到博客系统', 'https://picsum.photos/1920/500?random=1', '/blogArticle/1', 1, 1),
       ('技术分享平台', 'https://picsum.photos/1920/500?random=2', '/blogArticle/2', 2, 1),
       ('知识创造价值', 'https://picsum.photos/1920/500?random=3', '/blogArticle/3', 3, 1);
