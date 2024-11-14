package com.selflearntech.tech_blog_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

//	 @Bean
//	 CommandLineRunner run(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
//	 	return (args) -> {
//
//	 		if (roleRepository.findByAuthority(RoleType.ADMIN).isPresent()) return;
//
//	 		Role adminRole = roleRepository.save(Role.builder().authority(RoleType.ADMIN).build());
//
//	 		Set<Role> roles = new HashSet<>();
//	 		roles.add(adminRole);
//
//	 		User adminUser = User.builder()
//					.firstName("John")
//					.lastName("Doe")
//					.email("admin@gmail.com")
//					.password(passwordEncoder.encode("C11l08a#0522"))
//					.authorities(roles).build();
//			 adminUser.setToken(Token.builder().user(adminUser).token(null).build());
//	 		userRepository.save(adminUser);
//	 	};
//	 }

}
