package com.fashionkart.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fashionkart.dto.RegisterRequest;
import com.fashionkart.service.UserService;


import jakarta.validation.Valid;

@Controller
public class UserController {
	
	private UserService userService;
	
	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/register")
	public String register(Model model) {
		System.out.println("UserController.register()");
		model.addAttribute("registerRequest" , new RegisterRequest());
		
		return "customer/register";
	}
	
	@GetMapping("/login")
	public String login(@RequestParam(required = false) boolean success, Model model) {
		
		if(Boolean.TRUE.equals(success)) {
			model.addAttribute("successMessage","Registration successfull! please login.");
		}
		
		return "customer/login";
	}
	
	@PostMapping("/register")
	public String registerUser(@Valid @ModelAttribute("registerRequest") RegisterRequest request, Model model) {
		try {
			userService.registerUser(request);
			
			return "redirect:/login?success=true";
		}
		catch (RuntimeException e) {

		    System.out.println("ERROR OCCURRED: " + e.getMessage());

		    model.addAttribute("errorMessage", e.getMessage());

		    return "customer/register";
		}
	}
}
