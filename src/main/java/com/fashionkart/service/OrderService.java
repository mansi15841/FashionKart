package com.fashionkart.service;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashionkart.entity.Address;
import com.fashionkart.entity.Cart;
import com.fashionkart.entity.CartItem;
import com.fashionkart.entity.Order;
import com.fashionkart.entity.OrderItem;
import com.fashionkart.entity.Product;
import com.fashionkart.entity.ProductSize;
import com.fashionkart.entity.User;
import com.fashionkart.repository.AddressRepository;
import com.fashionkart.repository.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final CartService cartService;

    public OrderService(
            OrderRepository orderRepository,
            AddressRepository addressRepository,
            CartService cartService) {
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
        this.cartService = cartService;
    }

    @Transactional
    public Order placeOrder(User user, Long addressId, String paymentMethod) {

        // 1. Validate payment method
        if (paymentMethod == null ||
                !java.util.Set.of(
                        "COD", "UPI", "CARD",
                        "WALLET", "EMI", "NET_BANKING"
                ).contains(paymentMethod)) {
            throw new IllegalArgumentException("Please select a valid payment method.");
        }

        // 2. Get the customer's cart
        Cart cart = cartService.getOrCreateCart(user);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Your cart is empty.");
        }

        // 3. Validate delivery address belongs to this customer
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Please select a valid delivery address."));

        if (address.getUser() == null ||
                !address.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Invalid delivery address.");
        }

        // 4. Validate stock before changing anything
        for (CartItem cartItem : cart.getItems()) {

            if (cartItem.getQuantity() <= 0) {
                throw new IllegalArgumentException("Invalid item quantity in cart.");
            }

            ProductSize selectedSize = cartItem.getProductSize();

            if (selectedSize != null &&
                    selectedSize.getStockQuantity() < cartItem.getQuantity()) {
                throw new IllegalArgumentException(
                        "Insufficient stock for "
                                + cartItem.getProduct().getProductName()
                                + " (Size " + selectedSize.getSize() + ").");
            }
        }

        // 5. Calculate order totals
        double totalMrp = 0.0;
        double totalDiscount = 0.0;
        double subtotal = 0.0;

        for (CartItem cartItem : cart.getItems()) {

            double sellingPrice = cartItem.getPrice();
            int quantity = cartItem.getQuantity();

            Product product = cartItem.getProduct();
            double discountPercent = product.getDiscount();

            double originalPrice = sellingPrice;

            if (discountPercent > 0 && discountPercent < 100) {
                originalPrice = sellingPrice / (1 - discountPercent / 100.0);
            }

            totalMrp += originalPrice * quantity;
            totalDiscount += (originalPrice - sellingPrice) * quantity;
            subtotal += sellingPrice * quantity;
        }

        double shippingCharges = 100.0;
        double totalAmount = subtotal + shippingCharges;

        // Round monetary values to two decimal places
        totalMrp = roundMoney(totalMrp);
        totalDiscount = roundMoney(totalDiscount);
        subtotal = roundMoney(subtotal);
        totalAmount = roundMoney(totalAmount);

        // 6. Create the order
        Order order = new Order();
        order.setOrderNumber(
                "FK" + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 12)
                        .toUpperCase()
        );

        order.setUser(user);
        order.setAddress(address);
        order.setTotalMrp(totalMrp);
        order.setTotalDiscount(totalDiscount);
        order.setShippingCharges(shippingCharges);
        order.setTotalAmount(totalAmount);
        order.setPaymentMethod(paymentMethod);
        order.setOrderStatus("Processing");

        // No payment gateway is connected yet.
        // Never mark online payments as paid without verification.
        order.setPaymentStatus(
                "COD".equals(paymentMethod) ? "Pending" : "Payment Pending"
        );

        // 7. Copy cart items into order items and reduce stock
        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getProductName());

            String productImage = null;
            if (product.getImages() != null &&
                    !product.getImages().isEmpty()) {
                productImage = product.getImages().get(0).getImagePath();
            }

            orderItem.setProductImage(productImage);

            ProductSize selectedSize = cartItem.getProductSize();
            if (selectedSize != null) {
                orderItem.setSize(selectedSize.getSize());
                selectedSize.setStockQuantity(
                        selectedSize.getStockQuantity() - cartItem.getQuantity()
                );
            }

            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setSubtotal(
                    roundMoney(cartItem.getPrice() * cartItem.getQuantity())
            );

            order.getItems().add(orderItem);
        }

        // 8. Save order and its items
        Order savedOrder = orderRepository.save(order);

	     // Clear cart items after successfully saving the order
	     for (CartItem item : new ArrayList<>(cart.getItems())) {
	         cart.getItems().remove(item);
	         item.setCart(null);
	     }

     return savedOrder;
    }

    private double roundMoney(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }
}

