package com.fashionkart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.fashionkart.service.UserService;

@Configuration
public class SecurityConfig {
	
	private final CustomAuthenticationHandler authenticationHandler;
	private final PasswordEncoder passwordEncoder;
	private UserService userService;
	
	public SecurityConfig(UserService userService, PasswordEncoder passwordEncoder, CustomAuthenticationHandler authenticationHandler) {
		// TODO Auto-generated constructor stub
		this.userService = userService;
		this.passwordEncoder = passwordEncoder;
		this.authenticationHandler = authenticationHandler;
	}
	
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
		
		http
		.authenticationProvider(authenticationProvider())
		.authorizeHttpRequests(auth -> auth.requestMatchers("/","/login","/register","/admin/login",
				"/css/**","/js/**","/images/**","/men","/women","/kids","/products","/sale","/product-details","/uploads/**")
				.permitAll()
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.anyRequest().authenticated())
		
		.formLogin(form -> form
				.loginPage("/login")
				.loginProcessingUrl("/login")
				.usernameParameter("email")
				.passwordParameter("password")
				.successHandler(authenticationHandler)
				.failureHandler(authenticationHandler)
				.permitAll()
				)
		
		.logout(logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessHandler(authenticationHandler)
				.permitAll());
		
		return http.build();
	}
	
	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
		provider.setPasswordEncoder(passwordEncoder);
		
		return provider;
	}
	
}
