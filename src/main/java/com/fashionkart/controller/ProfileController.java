package com.fashionkart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fashionkart.entity.Address;
import com.fashionkart.entity.User;
import com.fashionkart.repository.UserRepository;
import com.fashionkart.service.AddressService;
import com.fashionkart.service.ProfileService;

@Controller
public class ProfileController {
	
	private ProfileService profileService;
	private AddressService addressService;
	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	
	public ProfileController(ProfileService profileService, AddressService addressService, UserRepository userRepository,
			PasswordEncoder passwordEncoder) {
		this.profileService = profileService;
		this.addressService = addressService;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		// TODO Auto-generated constructor stub
	}
	
//	get profile page
	
	@GetMapping("/profile")
	public String profile(
	        @RequestParam(required = false) boolean success,
	        @RequestParam(required = false) boolean passwordSuccess,
	        @RequestParam(required = false) boolean addressSuccess,
	        Authentication authentication,
	        Model model) {

		if(authentication == null || !authentication.isAuthenticated()) {
			return "redirect:/login";
		}
		
		String email = authentication.getName();
		
	    User user = userRepository.findByEmail(email);

	    if (user == null) {
	        return "redirect:/login";
	    }

	    model.addAttribute("user", user);
	    model.addAttribute("addresses",addressService.getuserAddresses(user));
	  
	    if (Boolean.TRUE.equals(success)) {
	        model.addAttribute("successMessage",
	                "Profile updated successfully!");
	    }

	    if (Boolean.TRUE.equals(passwordSuccess)) {
	        model.addAttribute("passwordSuccessMessage",
	                "Password changed successfully!");
	    }
	    
	    if(Boolean.TRUE.equals(addressSuccess)) {
	    	model.addAttribute("addressSuccessMessage","Address Updated Successfully!");
	    }

	    return "customer/profile";
	}
	
//	update existing profile
	
	@PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String contact,
            Authentication authentication,
            Model model) {

		if(authentication == null || !authentication.isAuthenticated()) {
			return "redirect:/login";
		}
		String loggedInEmail = authentication.getName();
        User user = userRepository.findByEmail(loggedInEmail);

        if (user == null) {
            return "redirect:/login";
        }

        try {

            User updatedUser = profileService.updateProfile(
                    user,
                    fullName,
                    email,
                    contact
            );

            return "redirect:/profile?success=true";

        } catch (RuntimeException e) {

            model.addAttribute("user", user);
            model.addAttribute("addresses", addressService.getuserAddresses(user));
            model.addAttribute("errorMessage", e.getMessage());

            return "customer/profile";
        }
    }
	
//	change existing password
	
	@PostMapping("/profile/change-password")
	public String changePassword(@RequestParam String currentPassword, @RequestParam String newPassword, 
			@RequestParam String confirmPassword, Authentication authentication, Model model){
		
		if(authentication == null || !authentication.isAuthenticated()) {
			return "redirect:/login";
		}
		
		String email = authentication.getName();
		User user = userRepository.findByEmail(email);
		
		if(user == null) {
			return "redirect:/login";
		}
		
		try {
			
			if(!passwordEncoder.matches(currentPassword, user.getPassword())) {
				throw new RuntimeException("current password is incorrect");
			}
			
			if(!newPassword.equals(confirmPassword)) {
				throw new RuntimeException("New Password and confirm password does not match");
			}
			
			if(newPassword.equals(currentPassword)) {
				throw new RuntimeException("New passowrd must be different from current password");
			}
			
			profileService.updatePassword(user, newPassword);
			
			return "redirect:/profile?passwordSuccess=true";
		} 
		catch (RuntimeException e) {
			
			model.addAttribute("user",user);
			model.addAttribute("addresses", addressService.getuserAddresses(user));
			model.addAttribute("passwordError",e.getMessage());
			
			return "customer/profile";
		}
	}
	
//	add address
	
	@PostMapping("profile/address/save")
	public String saveAddress(@ModelAttribute(name="addressData") Address address, Authentication authentication) {
		
		String email = authentication.getName();
		
		User user = userRepository.findByEmail(email); 
		
		if(user == null) {
			return "redirect:/login";
		}
		
		address.setUser(user);
		addressService.saveAddress(address);
		
		return "redirect:/profile";
	}
	
//	update existing address
	
	@PostMapping("/profile/address/update")
	public String updateAddress(@ModelAttribute("addressData") Address address, Authentication authentication) {

		String email = authentication.getName();
		User user = userRepository.findByEmail(email);
		
		if(user == null) {
			return "redirect:/login";
		}
		
		addressService.updateAddress(address, user);
		
		return "redirect:/profile?addressSuccess=true";
	}
	
//	delete existing address
	@PostMapping("/profile/address/delete")
	public String deleteAddress(@RequestParam Long addressId, Authentication authentication) {
		
		String email = authentication.getName();
		
		 User user = userRepository.findByEmail(email);
		
		if(user == null) {
			return "redirect:/login";
		}
		
		addressService.deleteAddress(addressId, user);
		
		return "redirect:/profile";
	}
}
