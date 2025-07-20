package com.taskmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing; // Enable JPA Auditing for createdAt/updatedAt

@SpringBootApplication
@EnableJpaAuditing // Enables automatic population of @CreatedDate and @LastModifiedDate
@EnableAspectJAutoProxy(proxyTargetClass = true) // Needed for AOP, e.g., security annotations
public class TaskManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskManagerApplication.class, args);
	}

}