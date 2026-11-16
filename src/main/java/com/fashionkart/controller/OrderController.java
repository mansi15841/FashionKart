package com.fashionkart.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fashionkart.entity.Order;
import com.fashionkart.entity.User;
import com.fashionkart.repository.OrderRepository;
import com.fashionkart.repository.UserRepository;
import com.fashionkart.service.OrderService;

import jakarta.servlet.http.HttpSession;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderController(
            OrderService orderService,
            OrderRepository orderRepository,
            UserRepository userRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    // Place order after clicking CONFIRM ORDER
    @PostMapping("/place-order")
    public String placeOrder(
            @RequestParam("paymentMethod") String paymentMethod,
            Authentication authentication,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getName())) {
            return "redirect:/login";
        }	

        User user = userRepository.findByEmail(authentication.getName());

        if (user == null) {
            return "redirect:/login";
        }

        Object selectedAddress = session.getAttribute("checkoutAddressId");

        if (!(selectedAddress instanceof Long addressId)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please select your delivery address first."
            );
            return "redirect:/checkout/address";
        }

        try {
            Order order = orderService.placeOrder(
                    user, addressId, paymentMethod
            );

            session.removeAttribute("checkoutAddressId");

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Order Placed Successfully! Order Number: "
                            + order.getOrderNumber()
            );

            return "redirect:/orders";

        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", ex.getMessage()
            );
            return "redirect:/checkout/payment";
        }
    }

    // Display orders belonging to the logged-in customer
    @GetMapping("/orders")
    public String orders(
            Authentication authentication,
            Model model) {

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getName())) {
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(authentication.getName());

        if (user == null) {
            return "redirect:/login";
        }

        List<Order> orders =
                orderRepository.findByUserOrderByOrderDateDesc(user);

        model.addAttribute("orders", orders);

        return "customer/orders";
    }
    

    // Display details of the selected customer's order
    @GetMapping("/order-details")
    public String orderDetails(
            @RequestParam("id") Long orderId,
            Authentication authentication,
            Model model) {

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getName())) {
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(authentication.getName());

        if (user == null) {
            return "redirect:/login";
        }

        Order order = orderRepository
                .findByIdAndUserId(orderId, user.getId())
                .orElse(null);

        if (order == null) {
            return "redirect:/orders";
        }

        model.addAttribute("order", order);

        return "customer/order-details";
    }
}
