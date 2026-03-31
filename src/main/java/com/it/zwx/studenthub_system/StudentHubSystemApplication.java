package com.it.zwx.studenthub_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement//开启事务功能
public class StudentHubSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudentHubSystemApplication.class, args);
    }

}
