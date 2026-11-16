package com.fashionkart.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fashionkart.entity.Address;
import com.fashionkart.entity.Cart;
import com.fashionkart.entity.CartItem;
import com.fashionkart.entity.User;
import com.fashionkart.repository.AddressRepository;
import com.fashionkart.repository.UserRepository;
import com.fashionkart.service.AddressService;
import com.fashionkart.service.CartService;

import jakarta.servlet.http.HttpSession;

@Controller
public class CheckoutController {

    private static final double SHIPPING_CHARGES = 100.0;

    private final UserRepository userRepository;
    private final AddressService addressService;
    private final CartService cartService;
    private final AddressRepository addressRepository;

    public CheckoutController(
            UserRepository userRepository,
            AddressService addressService,
            CartService cartService,
            AddressRepository addressRepository) {

        this.userRepository = userRepository;
        this.addressService = addressService;
        this.cartService = cartService;
        this.addressRepository = addressRepository;
    }

    @GetMapping("/checkout/address")
    public String showCheckoutAddress(
            @RequestParam(required = false, defaultValue = "false")
            boolean selected,

            @RequestParam(required = false)
            String error,

            Authentication authentication,
            Model model,
            HttpSession session) {

        // Verify login
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {

            return "redirect:/login";
        }

        User user = userRepository.findByEmail(authentication.getName());

        if (user == null) {
            return "redirect:/login";
        }

        // Get customer's cart
        Cart cart = cartService.getOrCreateCart(user);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        // Calculate price details
        double totalMrp = 0.0;
        double totalDiscount = 0.0;

        for (CartItem item : cart.getItems()) {

            double sellingPrice = item.getPrice();
            double discountPercent = item.getProduct().getDiscount();
            int quantity = item.getQuantity();

            double originalPrice;

            if (discountPercent > 0 && discountPercent < 100) {
                originalPrice =
                        sellingPrice / (1 - discountPercent / 100.0);
            } else {
                originalPrice = sellingPrice;
            }

            totalMrp += originalPrice * quantity;
            totalDiscount += (originalPrice - sellingPrice) * quantity;
        }

        // Use the cart's existing subtotal calculation
        double subtotal = cartService.calculateSubtotal(cart);

        double shippingCharges =
                cart.getItems().isEmpty() ? 0.0 : SHIPPING_CHARGES;

        double totalAmount = subtotal + shippingCharges;

        // Round monetary values to two decimal places
        totalMrp = roundMoney(totalMrp);
        totalDiscount = roundMoney(totalDiscount);
        subtotal = roundMoney(subtotal);
        shippingCharges = roundMoney(shippingCharges);
        totalAmount = roundMoney(totalAmount);

        // Send price values to Thymeleaf
        model.addAttribute("totalMrp", totalMrp);
        model.addAttribute("totalDiscount", totalDiscount);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingCharges", shippingCharges);
        model.addAttribute("totalAmount", totalAmount);

        // Send address and checkout data
        List<Address> addresses = addressService.getuserAddresses(user);

        model.addAttribute("addresses", addresses);
        model.addAttribute("addressData", new Address());
        model.addAttribute("activeStep", "address");
        model.addAttribute("cart", cart);
        model.addAttribute(
                "selectedAddressId",
                session.getAttribute("checkoutAddressId"));

        if (selected) {
            model.addAttribute(
                    "successMessage",
                    "Delivery address selected successfully!");
        }

        if ("invalidAddress".equals(error)) {
            model.addAttribute(
                    "errorMessage",
                    "Invalid address selection. Please select your own saved address.");
        }

        return "customer/checkout-address";
    }

    @PostMapping("/checkout/address/select")
    public String selectCheckoutAddress(
            @RequestParam Long addressId,
            Authentication authentication,
            HttpSession session) {

        // Verify login
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {

            return "redirect:/login";
        }

        User user = userRepository.findByEmail(authentication.getName());

        if (user == null) {
            return "redirect:/login";
        }

        // Ensure cart is not empty
        Cart cart = cartService.getOrCreateCart(user);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        // Find the selected address
        Address address = addressRepository.findById(addressId)
                .orElse(null);

        // Ensure the address belongs to the logged-in customer
        if (address == null
                || address.getUser() == null
                || !address.getUser().getId().equals(user.getId())) {

            return "redirect:/checkout/address?error=invalidAddress";
        }

        // Save selected address in session
        session.setAttribute("checkoutAddressId", address.getId());

        return "redirect:/checkout/payment";
    }
    
    @GetMapping("/checkout/payment")
    public String showPaymentPage(
            Authentication authentication,
            HttpSession session,
            Model model) {

        // Verify login
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(authentication.getName());

        if (user == null) {
            return "redirect:/login";
        }

        // Get customer's cart
        Cart cart = cartService.getOrCreateCart(user);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        // Ensure that a delivery address has been selected
        Object selectedAddressId = session.getAttribute("checkoutAddressId");

        if (selectedAddressId == null) {
            return "redirect:/checkout/address";
        }

        Address address = addressRepository.findById((Long) selectedAddressId)
                .orElse(null);

        if (address == null
                || address.getUser() == null
                || !address.getUser().getId().equals(user.getId())) {
            session.removeAttribute("checkoutAddressId");
            return "redirect:/checkout/address?error=invalidAddress";
        }

        // Calculate price details using existing cart logic
        double totalMrp = 0.0;
        double totalDiscount = 0.0;

        for (CartItem item : cart.getItems()) {
            double sellingPrice = item.getPrice();
            double discountPercent = item.getProduct().getDiscount();
            int quantity = item.getQuantity();

            double originalPrice;

            if (discountPercent > 0 && discountPercent < 100) {
                originalPrice = sellingPrice / (1 - discountPercent / 100.0);
            } else {
                originalPrice = sellingPrice;
            }

            totalMrp += originalPrice * quantity;
            totalDiscount += (originalPrice - sellingPrice) * quantity;
        }

        double subtotal = cartService.calculateSubtotal(cart);
        double shippingCharges = SHIPPING_CHARGES;
        double totalAmount = subtotal + shippingCharges;

        // Round monetary values
        totalMrp = roundMoney(totalMrp);
        totalDiscount = roundMoney(totalDiscount);
        subtotal = roundMoney(subtotal);
        shippingCharges = roundMoney(shippingCharges);
        totalAmount = roundMoney(totalAmount);

        // Send data to payment.html
        model.addAttribute("cart", cart);
        model.addAttribute("address", address);
        model.addAttribute("totalMrp", totalMrp);
        model.addAttribute("totalDiscount", totalDiscount);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingCharges", shippingCharges);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("activeStep", "payment");

        return "customer/payment";
    }

    private double roundMoney(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }
}
