package com.fashionkart.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fashionkart.entity.Cart;
import com.fashionkart.entity.CartItem;
import com.fashionkart.entity.User;
import com.fashionkart.service.CartService;
import com.fashionkart.service.UserService;

@Controller
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    // Fixed shipping charge
    private static final double SHIPPING_CHARGES = 100.0;

    public CartController(
            CartService cartService,
            UserService userService) {

        this.cartService = cartService;
        this.userService = userService;
    }

    // ==================================================
    // GET LOGGED-IN USER
    // ==================================================

    private User getLoggedInUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(
                        String.valueOf(authentication.getPrincipal()))) {

            return null;
        }

        return userService.findByEmail(authentication.getName());
    }

    // ==================================================
    // VIEW CART
    // ==================================================

    @GetMapping("/cart")
    public String cart(
            Model model,
            RedirectAttributes redirectAttributes) {

        User user = getLoggedInUser();

        if (user == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Please login to view your cart");

            return "redirect:/login";
        }

        Cart cart = cartService.getOrCreateCart(user);

        // Calculate the total MRP, discount and subtotal.
        double totalMrp = 0.0;
        double totalDiscount = 0.0;

        if (cart.getItems() != null) {

            for (CartItem item : cart.getItems()) {

                double sellingPrice = item.getPrice();
                double discountPercent =
                        item.getProduct().getDiscount();

                double originalPrice;

                if (discountPercent > 0 && discountPercent < 100) {

                    originalPrice =
                            sellingPrice / (1 - discountPercent / 100.0);

                } else {

                    originalPrice = sellingPrice;
                }

                totalMrp += originalPrice * item.getQuantity();

                totalDiscount +=
                        (originalPrice - sellingPrice)
                                * item.getQuantity();
            }
        }

        double subtotal = cartService.calculateSubtotal(cart);

        double totalAmount = subtotal + SHIPPING_CHARGES;

        // Round monetary amounts to two decimal places.
        totalMrp = roundMoney(totalMrp);
        totalDiscount = roundMoney(totalDiscount);
        subtotal = roundMoney(subtotal);
        totalAmount = roundMoney(totalAmount);

        model.addAttribute("cart", cart);

        model.addAttribute("totalMrp", totalMrp);
        model.addAttribute("totalDiscount", totalDiscount);
        model.addAttribute("subtotal", subtotal);

        model.addAttribute(
                "shippingCharges",
                cart.getItems() == null || cart.getItems().isEmpty()
                        ? 0.0
                        : SHIPPING_CHARGES);

        model.addAttribute(
                "totalAmount",
                cart.getItems() == null || cart.getItems().isEmpty()
                        ? 0.0
                        : totalAmount);

        // Checkout progress indicator
        model.addAttribute("activeStep", "cart");

        return "customer/cart";
    }

    private double roundMoney(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }

    // ==================================================
    // PROCEED TO ADDRESS
    // ==================================================

    @PostMapping("/checkout/address")
    public String proceedToAddress(
            RedirectAttributes redirectAttributes) {

        User user = getLoggedInUser();

        if (user == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Please login before placing your order");

            return "redirect:/login";
        }

        Cart cart = cartService.getOrCreateCart(user);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Your cart is empty");

            return "redirect:/cart";
        }

        // Address page will be implemented next.
        return "redirect:/checkout/address";
    }

    // ==================================================
    // ADD TO CART
    // ==================================================

    @PostMapping("/cart/add")
    public String addToCart(
            @RequestParam Long productId,
            @RequestParam(required = false) String size,
            @RequestParam(defaultValue = "1") int quantity,
            RedirectAttributes redirectAttributes) {

        User user = getLoggedInUser();

        if (user == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Please login to add products to your cart");

            return "redirect:/login";
        }

        String result = cartService.addToCart(
                user,
                productId,
                size,
                quantity);

        if ("success".equals(result)) {

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Product added to cart");

            redirectAttributes.addFlashAttribute(
                    "cartItemCount",
                    cartService.getCartItemCount(user));

        } else {

            redirectAttributes.addFlashAttribute(
                    "error",
                    result);
        }

        // Stay on the product details page.
        return "redirect:/product-details?id=" + productId;
    }

    // ==================================================
    // UPDATE QUANTITY
    // ==================================================

    @PostMapping("/cart/update")
    public String updateQuantity(
            @RequestParam Long cartItemId,
            @RequestParam int quantity,
            RedirectAttributes redirectAttributes) {

        User user = getLoggedInUser();

        if (user == null) {
            return "redirect:/login";
        }

        String result = cartService.updateQuantity(
                user,
                cartItemId,
                quantity);

        if (!"success".equals(result)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    result);
        }

        return "redirect:/cart";
    }

    // ==================================================
    // REMOVE ITEM
    // ==================================================

    @PostMapping("/cart/remove")
    public String removeItem(
            @RequestParam Long cartItemId,
            RedirectAttributes redirectAttributes) {

        User user = getLoggedInUser();

        if (user == null) {
            return "redirect:/login";
        }

        String result = cartService.removeItem(user, cartItemId);

        if (!"success".equals(result)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    result);
        }

        return "redirect:/cart";
    }
}
