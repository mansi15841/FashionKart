package com.fashionkart.config;

import java.io.IOException;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationHandler implements AuthenticationSuccessHandler , AuthenticationFailureHandler,
LogoutSuccessHandler{

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		// TODO Auto-generated method stub
		
		if(authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
			
			response.sendRedirect("/FashionKart/admin/dashboard");
		}
		else {
			response.sendRedirect("/FashionKart");
		}
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		// TODO Auto-generated method stub
		String referer = request.getHeader("Referer");
		
		
		if(referer != null && referer.contains("FashionKart/admin/login")) {
			response.sendRedirect("/FashionKart/admin/login?error=true");
		}
		else {
			response.sendRedirect("/FashionKart/login?error=true");
		}
	}

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
			@Nullable Authentication authentication) throws IOException, ServletException {
		// TODO Auto-generated method stub
		String loginType = request.getParameter("loginType");
		if("admin".equalsIgnoreCase(loginType)) {
			response.sendRedirect("/FashionKart/admin/login");
		}
		else {
			response.sendRedirect("/FashionKart/login");
		}
	}
	
	
	
}
