package com.fashionkart.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.fashionkart.entity.Cart;
import com.fashionkart.entity.CartItem;
import com.fashionkart.entity.User;
import com.fashionkart.repository.UserRepository;
import com.fashionkart.service.CartService;

@ControllerAdvice(annotations = Controller.class)
public class GlobalControllerAdvice {

    private final UserRepository userRepository;
    private final CartService cartService;

    public GlobalControllerAdvice(
            UserRepository userRepository,
            CartService cartService) {

        this.userRepository = userRepository;
        this.cartService = cartService;
    }

    @ModelAttribute
    public void addCartItemCount(
            Model model,
            Authentication authentication) {

        int cartItemCount = 0;

        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {

            User user =
                    userRepository.findByEmail(authentication.getName());

            if (user != null) {

                Cart cart = cartService.getOrCreateCart(user);

                if (cart.getItems() != null) {
                    for (CartItem item : cart.getItems()) {
                        if (item.getQuantity() > 0) {
                            cartItemCount += item.getQuantity();
                        }
                    }
                }
            }
        }

        model.addAttribute("cartItemCount", cartItemCount);
    }
}
