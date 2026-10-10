CREATE DATABASE IF NOT EXISTS attendance_db;
USE attendance_db;
CREATE TABLE IF NOT EXISTS app_users(id INT AUTO_INCREMENT PRIMARY KEY,username VARCHAR(50) UNIQUE NOT NULL,password VARCHAR(100) NOT NULL,role VARCHAR(20) NOT NULL DEFAULT 'ADMIN');
CREATE TABLE IF NOT EXISTS students(id INT AUTO_INCREMENT PRIMARY KEY,roll_no VARCHAR(30) UNIQUE NOT NULL,full_name VARCHAR(100) NOT NULL,department VARCHAR(60) NOT NULL,year_level VARCHAR(20) NOT NULL);
CREATE TABLE IF NOT EXISTS attendance(id INT AUTO_INCREMENT PRIMARY KEY,student_id INT NOT NULL,attendance_date DATE NOT NULL,subject VARCHAR(100) NOT NULL,status ENUM('Present','Absent') NOT NULL,marked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE,UNIQUE KEY uq_student_date_subject(student_id,attendance_date,subject));
INSERT IGNORE INTO app_users(username,password,role) VALUES('admin','admin123','ADMIN');
INSERT IGNORE INTO students(roll_no,full_name,department,year_level) VALUES('AIML001','Ravi Kumar','CSE-AIML','III Year'),('AIML002','Sravya Reddy','CSE-AIML','III Year'),('AIML003','Arjun Rao','CSE-AIML','III Year');
