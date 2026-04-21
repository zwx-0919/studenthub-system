CREATE TABLE IF NOT EXISTS ai_consult (
  id INT PRIMARY KEY AUTO_INCREMENT,
  student_id INT NOT NULL,
  session_id VARCHAR(64) DEFAULT NULL,
  question VARCHAR(2000) NOT NULL,
  answer VARCHAR(4000) NOT NULL,
  create_time DATETIME NOT NULL,
  INDEX idx_ai_consult_student (student_id),
  INDEX idx_ai_consult_session (session_id),
  INDEX idx_ai_consult_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

