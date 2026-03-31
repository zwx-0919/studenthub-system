CREATE TABLE IF NOT EXISTS leave_apply (
  id INT PRIMARY KEY AUTO_INCREMENT,
  student_id INT NOT NULL,
  counselor_id INT NOT NULL,
  class_no VARCHAR(50) NOT NULL,
  leave_type INT NOT NULL,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  reason VARCHAR(1000) NOT NULL,
  proof_url VARCHAR(1000) NULL,
  approve_status INT NOT NULL DEFAULT 0,
  approve_remark VARCHAR(1000) NULL,
  approve_time DATETIME NULL,
  create_time DATETIME NOT NULL,
  update_time DATETIME NULL,
  INDEX idx_leave_student (student_id),
  INDEX idx_leave_counselor (counselor_id),
  INDEX idx_leave_class (class_no),
  INDEX idx_leave_status (approve_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

