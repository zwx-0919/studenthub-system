CREATE TABLE IF NOT EXISTS post_comment (
  id INT PRIMARY KEY AUTO_INCREMENT,
  post_id INT NOT NULL,
  user_id INT NOT NULL,
  content VARCHAR(2000) NOT NULL,
  create_time DATETIME NOT NULL,
  INDEX idx_post_comment_post (post_id),
  INDEX idx_post_comment_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
