package com.fashionkart.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashionkart.entity.Cart;
import com.fashionkart.entity.CartItem;
import com.fashionkart.entity.Product;
import com.fashionkart.entity.ProductSize;
import com.fashionkart.entity.User;
import com.fashionkart.repository.AddressRepository;
import com.fashionkart.repository.CartItemRepository;
import com.fashionkart.repository.CartRepository;
import com.fashionkart.repository.ProductRepository;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository) {

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    // Get existing cart or create a new one
    @Transactional
    public Cart getOrCreateCart(User user) {

        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    // Get the selling price already stored in the product.
    // Product.price is already the discounted selling price.
    private double calculateDiscountedPrice(Product product) {

        return Math.round(product.getPrice() * 100.0) / 100.0;
    }

    // Add product to cart
    @Transactional
    public String addToCart(User user, Long productId, String size, int quantity) {

        // 1. Find product
        Product product = productRepository.findById(productId).orElse(null);

        if (product == null) {
            return "Product not found";
        }

        // 2. Validate quantity
        if (quantity < 1) {
            quantity = 1;
        }

        // 3. Get the existing selling price without applying
        // the discount a second time
        double sellingPrice = calculateDiscountedPrice(product);

        // 4. Get user's cart
        Cart cart = getOrCreateCart(user);

        // 5. Find selected size
        ProductSize selectedSize = null;

        if (size != null && !size.isBlank()&& product.getSizes() != null) {

            for (ProductSize productSize : product.getSizes()) {

                if (productSize.getSize() != null
                        && productSize.getSize()
                                .equalsIgnoreCase(size)) {

                    selectedSize = productSize;
                    break;
                }
            }
        }

        // 6. Require size if the product has available sizes
        if (product.getSizes() != null && !product.getSizes().isEmpty() && selectedSize == null) {

            return "Please select a size";
        }

        // 7. Check stock
        if (selectedSize != null) {

            int stock = selectedSize.getStockQuantity();

            if (stock <= 0) {
                return "Selected size is out of stock";
            }

            if (quantity > stock) {
                return "Only " + stock + " item(s) available";
            }
        }

        // 8. Find the same product and same size in cart
        CartItem existingItem = null;

        if (cart.getItems() != null) {

            for (CartItem item : cart.getItems()) {

                boolean sameProduct = item.getProduct().getId().equals(productId);

                boolean sameSize = false;

                if (item.getProductSize() == null && selectedSize == null) {

                    sameSize = true;

                } else if (item.getProductSize() != null && selectedSize != null && item.getProductSize().getId()
                                .equals(selectedSize.getId())) {

                    sameSize = true;
                }

                if (sameProduct && sameSize) {
                    existingItem = item;
                    break;
                }
            }
        }

        // 9. Existing item: increase quantity
        if (existingItem != null) {

            int newQuantity =
                    existingItem.getQuantity() + quantity;

            if (selectedSize != null
                    && newQuantity >
                            selectedSize.getStockQuantity()) {

                return "Only "
                        + selectedSize.getStockQuantity()
                        + " item(s) available";
            }

            existingItem.setQuantity(newQuantity);

            // Save the current selling price without another discount
            existingItem.setPrice(sellingPrice);

            cartItemRepository.save(existingItem);
        }

        // 10. New item: create cart item
        else {

            CartItem cartItem = new CartItem();

            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setProductSize(selectedSize);
            cartItem.setQuantity(quantity);

            // Save the current selling price without another discount
            cartItem.setPrice(sellingPrice);

            cart.getItems().add(cartItem);

            cartItemRepository.save(cartItem);
        }

        cartRepository.save(cart);

        return "success";
    }

    // Update cart quantity
    @Transactional
    public String updateQuantity(User user, Long cartItemId, int quantity) {

        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElse(null);

        if (item == null
                || !item.getCart().getId().equals(cart.getId())) {

            return "Cart item not found";
        }

        if (quantity < 1) {
            return "Quantity must be at least 1";
        }

        ProductSize productSize = item.getProductSize();

        if (productSize != null
                && quantity > productSize.getStockQuantity()) {

            return "Only "
                    + productSize.getStockQuantity()
                    + " item(s) available";
        }

        item.setQuantity(quantity);

        cartItemRepository.save(item);

        return "success";
    }

    // Remove item from cart
    @Transactional
    public String removeItem(User user, Long cartItemId) {

        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElse(null);

        if (item == null
                || !item.getCart().getId().equals(cart.getId())) {

            return "Cart item not found";
        }

        cart.getItems().remove(item);

        cartItemRepository.delete(item);

        return "success";
    }

    // Calculate subtotal using saved selling prices
    @Transactional(readOnly = true)
    public double calculateSubtotal(Cart cart) {

        double subtotal = 0.0;

        if (cart.getItems() == null) {
            return subtotal;
        }

        for (CartItem item : cart.getItems()) {
            subtotal += item.getPrice() * item.getQuantity();
        }

        return Math.round(subtotal * 100.0) / 100.0;
    }

    // Get total quantity of products in cart
    @Transactional(readOnly = true)
    public int getCartItemCount(User user) {

        Cart cart = cartRepository.findByUser(user)
                .orElse(null);

        if (cart == null || cart.getItems() == null) {
            return 0;
        }

        int count = 0;

        for (CartItem item : cart.getItems()) {
            count += item.getQuantity();
        }

        return count;
    }
}
