package com.example.document_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import com.example.document_management.repository.IAdminUserRepository;
import com.example.document_management.model.AdminUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class DocumentManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocumentManagementApplication.class, args);
	}

	@Bean
	ApplicationRunner seedAdmin(IAdminUserRepository repo,
								PasswordEncoder encoder,
								@Value("${app.admin.email}") String email,
								@Value("${app.admin.password}") String password) {
		return args -> {
			if (repo.count() == 0) {
				AdminUser u = new AdminUser();
				u.setEmail(email);
				u.setPasswordHash(encoder.encode(password));
				repo.save(u);
			}
		};
	}
}

