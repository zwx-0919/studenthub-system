-- 新增：专业表
CREATE TABLE IF NOT EXISTS major_info (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 新增：班级表
CREATE TABLE IF NOT EXISTS class_info (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  major_id INT NOT NULL,
  INDEX idx_class_major (major_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户表增量字段（兼容旧表）
ALTER TABLE user
  ADD COLUMN IF NOT EXISTS class_id INT NULL,
  ADD COLUMN IF NOT EXISTS counselor_id INT NULL,
  ADD COLUMN IF NOT EXISTS charge_class_ids VARCHAR(255) NULL,
  ADD COLUMN IF NOT EXISTS charge_major_ids VARCHAR(255) NULL,
  ADD COLUMN IF NOT EXISTS identity_type INT NULL,
  ADD COLUMN IF NOT EXISTS user_avatar VARCHAR(500) NULL;

-- 课程表字段调整：classroom/class_time -> location/time，并新增major_id
ALTER TABLE course
  ADD COLUMN IF NOT EXISTS major_id INT NULL,
  ADD COLUMN IF NOT EXISTS location VARCHAR(255) NULL,
  ADD COLUMN IF NOT EXISTS `time` DATETIME NULL;

-- 考试表字段调整：exam_location/exam_time -> location/time，并新增major_id
ALTER TABLE exam
  ADD COLUMN IF NOT EXISTS major_id INT NULL,
  ADD COLUMN IF NOT EXISTS location VARCHAR(255) NULL,
  ADD COLUMN IF NOT EXISTS `time` DATETIME NULL;

-- 帖子表增量字段：图片地址
ALTER TABLE post
  ADD COLUMN IF NOT EXISTS image_url VARCHAR(500) NULL;

-- 帖子点赞（用户-帖子唯一）
CREATE TABLE IF NOT EXISTS post_like (
  id INT PRIMARY KEY AUTO_INCREMENT,
  post_id INT NOT NULL,
  user_id INT NOT NULL,
  create_time DATETIME NOT NULL,
  UNIQUE KEY uk_post_user (post_id, user_id),
  INDEX idx_post_like_post (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 评论二级回复
ALTER TABLE post_comment
  ADD COLUMN IF NOT EXISTS parent_id INT NULL,
  ADD COLUMN IF NOT EXISTS reply_to_user_id INT NULL;

-- 学业打卡学习时长（分钟）
ALTER TABLE study_check
  ADD COLUMN IF NOT EXISTS study_duration_minutes INT NULL DEFAULT 0;
