-- Exam table (exam)
-- 字段约定与后端 Entity 保持一致（course_id/exam_name/exam_time/exam_location/remind_time/status）

CREATE TABLE IF NOT EXISTS exam (
  id INT PRIMARY KEY AUTO_INCREMENT,
  course_id INT NOT NULL,
  exam_name VARCHAR(255) NOT NULL,
  `time` DATETIME NULL,
  location VARCHAR(255) NULL,
  remind_time DATETIME NULL,
  class_no VARCHAR(50) NOT NULL,
  major_id INT NULL,
  status INT NOT NULL DEFAULT 1,
  update_time DATETIME NULL,
  INDEX idx_exam_class_no (class_no),
  INDEX idx_exam_time (`time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

