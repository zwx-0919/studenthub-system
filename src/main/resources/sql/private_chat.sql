CREATE TABLE IF NOT EXISTS private_chat (
  id INT PRIMARY KEY AUTO_INCREMENT,
  sender_id INT NOT NULL,
  receiver_id INT NOT NULL,
  content VARCHAR(2000) NOT NULL,
  send_time DATETIME NOT NULL,
  is_read TINYINT NOT NULL DEFAULT 0,
  INDEX idx_private_chat_pair (sender_id, receiver_id),
  INDEX idx_private_chat_time (send_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
