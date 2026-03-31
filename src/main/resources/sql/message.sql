CREATE TABLE IF NOT EXISTS message (
  id INT PRIMARY KEY AUTO_INCREMENT,
  sender_id INT NOT NULL,
  receiver_id INT NOT NULL,
  message_type INT NOT NULL COMMENT '1系统提醒 2私信 3互动提醒',
  content VARCHAR(2000) NOT NULL,
  is_read INT NOT NULL DEFAULT 0,
  send_time DATETIME NOT NULL,
  biz_id INT NULL,
  INDEX idx_message_receiver (receiver_id),
  INDEX idx_message_read (is_read),
  INDEX idx_message_type (message_type),
  INDEX idx_message_time (send_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

