	package com.fashionkart.service;
	
	import org.springframework.security.core.userdetails.UserDetails;
	import org.springframework.security.core.userdetails.UserDetailsService;
	import org.springframework.security.core.userdetails.UsernameNotFoundException;
	import org.springframework.security.crypto.password.PasswordEncoder;
	import org.springframework.stereotype.Service;
	
	import com.fashionkart.dto.RegisterRequest;
	import com.fashionkart.entity.User;
	import com.fashionkart.repository.UserRepository;


	@Service
	public class UserService implements UserDetailsService{
	
		private UserRepository userRepository;
		
		private PasswordEncoder passwordEncoder;
		
		public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
			this.passwordEncoder = passwordEncoder;
			this.userRepository = userRepository;
		}
		
		@Override
		public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
			// TODO Auto-generated method stub
			User user = userRepository.findByEmail(email);
			
			if(user == null) {
				throw new UsernameNotFoundException("user not found");
			}
			
			return org.springframework.security.core.userdetails.User
					.withUsername(user.getEmail())
					.password(user.getPassword())
					.roles(user.getRole().toUpperCase())
					.build();
		}
	
	
		public User registerUser(RegisterRequest registerRequest) {
			if(userRepository.existsByEmail(registerRequest.getEmail())) {
				throw new RuntimeException("Email already registered");
			}
			if(userRepository.existsByContact(registerRequest.getContact())) {
				throw new RuntimeException("Contact already registered");
			}
			if(!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
				throw new RuntimeException("password do no match");
			}
			
			User user = new User();
			
			user.setFullName(registerRequest.getFullName());
			user.setContact(registerRequest.getContact());
			user.setEmail(registerRequest.getEmail());
			user.setGender(registerRequest.getGender());
			user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
			
			return userRepository.save(user);
		}
		
		public User findByEmail(String email) {
		    return userRepository.findByEmail(email);
		}
	
	}
