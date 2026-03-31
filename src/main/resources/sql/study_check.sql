CREATE TABLE IF NOT EXISTS study_check (
  id INT PRIMARY KEY AUTO_INCREMENT,
  student_id INT NOT NULL,
  counselor_id INT NOT NULL,
  class_no VARCHAR(50) NOT NULL,
  content VARCHAR(1000) NOT NULL,
  location VARCHAR(255) NULL,
  image_url VARCHAR(1000) NULL,
  status INT NOT NULL DEFAULT 1,
  check_time DATETIME NOT NULL,
  INDEX idx_study_check_student (student_id),
  INDEX idx_study_check_counselor (counselor_id),
  INDEX idx_study_check_class (class_no),
  INDEX idx_study_check_time (check_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

