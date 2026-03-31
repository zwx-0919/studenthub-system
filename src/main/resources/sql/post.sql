CREATE TABLE IF NOT EXISTS post (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  user_name VARCHAR(100) NOT NULL,
  category VARCHAR(20) NOT NULL COMMENT '求助/二手/失物/其他',
  title VARCHAR(120) NOT NULL,
  content VARCHAR(2000) NOT NULL,
  image_url VARCHAR(500) NULL,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '0待审核 1已发布 2已下架',
  like_count INT NOT NULL DEFAULT 0,
  comment_count INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  INDEX idx_post_user (user_id),
  INDEX idx_post_category (category),
  INDEX idx_post_status_time (status, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

