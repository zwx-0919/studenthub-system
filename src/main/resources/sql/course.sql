-- Course table (course)
-- 字段约定与后端 Entity 保持一致（course_name/course_number/teacher_name/class_time/classroom/term/status）

CREATE TABLE IF NOT EXISTS course (
  id INT PRIMARY KEY AUTO_INCREMENT,
  course_name VARCHAR(255) NOT NULL,
  course_number VARCHAR(100) NULL,
  teacher_name VARCHAR(255) NULL,
  `time` DATETIME NULL,
  location VARCHAR(255) NULL,
  term VARCHAR(100) NULL,
  class_no VARCHAR(50) NOT NULL,
  major_id INT NULL,
  status INT NOT NULL DEFAULT 1,
  update_time DATETIME NULL,
  INDEX idx_course_class_no (class_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

