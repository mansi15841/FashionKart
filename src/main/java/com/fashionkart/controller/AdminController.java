package com.fashionkart.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fashionkart.entity.Order;
import com.fashionkart.entity.Product;
import com.fashionkart.entity.ProductSize;
import com.fashionkart.entity.User;
import com.fashionkart.repository.OrderRepository;
import com.fashionkart.repository.ProductSizeRepository;
import com.fashionkart.repository.UserRepository;
import com.fashionkart.service.ProductService;
import com.fashionkart.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AdminController {

    private final UserService userService;
    private final ProductService productService;
    private final ProductSizeRepository productSizeRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public AdminController(
            UserService userService,
            ProductService productService,
            ProductSizeRepository productSizeRepository,
            OrderRepository orderRepository,
            UserRepository userRepository) {

        this.userService = userService;
        this.productService = productService;
        this.productSizeRepository = productSizeRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    // =========================================
    // ADMIN DASHBOARD
    // =========================================

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {

        long totalProducts = productService.getAllProducts().size();

        long totalOrders = orderRepository.count();

        // Count customer accounts only
        long totalUsers = userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() != null
                        && user.getRole().equalsIgnoreCase("User"))
                .count();

        // Latest five orders
        List<Order> recentOrders =
                orderRepository.findAllByOrderByOrderDateDesc(
                        PageRequest.of(0, 5));

        // Total sales excluding cancelled orders
        double totalSales = orderRepository.findAll()
                .stream()
                .filter(order -> order.getOrderStatus() != null)
                .filter(order ->
                        !order.getOrderStatus().equalsIgnoreCase("Cancelled"))
                .mapToDouble(Order::getTotalAmount)
                .sum();

        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalSales", totalSales);
        model.addAttribute("recentOrders", recentOrders);

        return "admin/dashboard";
    }

    // =========================================
    // ADMIN PRODUCTS
    // =========================================

    @GetMapping("/admin/products")
    public String adminProduct(
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model) {

        if (keyword == null || keyword.trim().isEmpty()) {
            model.addAttribute("products", productService.getAllProducts());
        } else {
            model.addAttribute("products", productService.searchProducts(keyword));
        }

        model.addAttribute("keyword", keyword);

        return "admin/products";
    }

    @GetMapping("/admin/products/add")
    public String addProduct() {
        return "admin/add-product";
    }

    @GetMapping("/admin/products/edit")
    public String editProduct(
            @RequestParam("id") Long id,
            Model model) {

        Product product = productService.getProductById(id);

        model.addAttribute("product", product);
        model.addAttribute(
                "availableSizes",
                List.of("XS", "S", "M", "L", "XL", "XXL"));

        return "admin/edit-product";
    }

    // =========================================
    // ADMIN LOGIN PAGE
    // =========================================

    @GetMapping("/admin/login")
    public String adminLogin() {
        return "admin/admin-login";
    }

    // =========================================
    // ADMIN ORDERS
    // =========================================

    @GetMapping("/admin/orders")
    public String adminOrders(Model model) {

        List<Order> orders =
                orderRepository.findAllByOrderByOrderDateDesc(
                        PageRequest.of(0, 100));

        model.addAttribute("orders", orders);

        model.addAttribute("totalOrders", orderRepository.count());

        model.addAttribute(
                "processingOrders",
                orders.stream()
                        .filter(order ->
                                "Processing".equalsIgnoreCase(order.getOrderStatus()))
                        .count());

        model.addAttribute(
                "shippedOrders",
                orders.stream()
                        .filter(order ->
                                "Shipped".equalsIgnoreCase(order.getOrderStatus()))
                        .count());

        model.addAttribute(
                "deliveredOrders",
                orders.stream()
                        .filter(order ->
                                "Delivered".equalsIgnoreCase(order.getOrderStatus()))
                        .count());

        return "admin/orders";
    }

    // =========================================
    // ADMIN ORDER DETAILS
    // =========================================

    @GetMapping("/admin/order-detail")
    public String adminOrderDetails(
            @RequestParam("id") String orderNumber,
            Model model) {

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Order not found"));

        model.addAttribute("order", order);

        return "admin/order-details";
    }

    // =========================================
    // ADMIN USERS
    // =========================================

    @GetMapping("/admin/users")
    public String adminUsers(Model model) {

        // Fetch customer accounts, excluding admin accounts
        List<User> users = userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() != null
                        && user.getRole().equalsIgnoreCase("User"))
                .toList();

        long totalUsers = users.size();

        // Count registrations in the current month
        java.time.YearMonth currentMonth =
                java.time.YearMonth.now();

        long newThisMonth = users.stream()
                .filter(user -> user.getCreatedAt() != null)
                .filter(user ->
                        java.time.YearMonth.from(user.getCreatedAt())
                                .equals(currentMonth))
                .count();

        // Blocking is not implemented yet.
        long blockedUsers = 0;
        long activeUsers = totalUsers - blockedUsers;

        model.addAttribute("users", users);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("newThisMonth", newThisMonth);
        model.addAttribute("blockedUsers", blockedUsers);

        return "admin/users";
    }

    // =========================================
    // ADMIN USER DETAILS
    // =========================================

    @GetMapping("/admin/user-detail")
    public String userDetails(@RequestParam Long id, Model model) {

        User user = userRepository.findById(id)
                .filter(u -> "User".equalsIgnoreCase(u.getRole()))
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Customer not found"));

        List<Order> orders = orderRepository
                .findByUserOrderByOrderDateDesc(user);

        long totalOrders = orders.size();

        long deliveredOrders = orders.stream()
                .filter(o -> "Delivered".equalsIgnoreCase(o.getOrderStatus()))
                .count();

        long processingOrders = orders.stream()
                .filter(o -> "Processing".equalsIgnoreCase(o.getOrderStatus()))
                .count();

        double totalSpent = orders.stream()
                .filter(o -> !"Cancelled".equalsIgnoreCase(o.getOrderStatus()))
                .mapToDouble(Order::getTotalAmount)
                .sum();

        model.addAttribute("user", user);
        model.addAttribute("orders", orders);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);
        model.addAttribute("processingOrders", processingOrders);
        model.addAttribute("totalSpent", totalSpent);

        return "admin/user-details";
    }

    // =========================================
    // ADD PRODUCT
    // =========================================

    @PostMapping("/admin/products/add")
    public String saveProduct(
            @ModelAttribute Product product,
            @RequestParam("status") String status,
            @RequestParam("productImages") MultipartFile[] productImages,
            HttpServletRequest request) throws IOException {

        product.setActive(status.equals("Active"));

        productService.saveProduct(product);

        productService.saveProductImages(
                productImages,
                product.getSku(),
                product);

        request.getParameterMap().forEach((key, value) -> {

            if (key.startsWith("sizeStock_")
                    && value.length > 0
                    && !value[0].isBlank()) {

                String size = key.substring("sizeStock_".length());

                int stock = Integer.parseInt(value[0]);

                ProductSize productSize = new ProductSize();

                productSize.setSize(size);
                productSize.setStockQuantity(stock);
                productSize.setProduct(product);

                productSizeRepository.save(productSize);
            }
        });

        return "redirect:/admin/products";
    }

    // =========================================
    // UPDATE PRODUCT
    // =========================================

    @PostMapping("/admin/products/update")
    public String updateProduct(
            @RequestParam("id") Long id,
            @ModelAttribute Product product,
            @RequestParam("status") String status,
            @RequestParam("productImages") MultipartFile[] productImages,
            HttpServletRequest request) throws IOException {

        Product existingProduct = productService.getProductById(id);

        existingProduct.setProductName(product.getProductName());
        existingProduct.setSku(product.getSku());
        existingProduct.setBrand(product.getBrand());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setSubCategory(product.getSubCategory());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setDiscount(product.getDiscount());
        existingProduct.setColor(product.getColor());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setActive(status.equals("Active"));

        productService.saveProduct(existingProduct);

        // Save newly uploaded images
        productService.saveProductImages(
                productImages,
                existingProduct.getSku(),
                existingProduct);

        // Remove unchecked sizes
        String[] selectedSizes =
                request.getParameterValues("selectedSizes");

        existingProduct.getSizes().removeIf(size -> {

            boolean selected = selectedSizes != null
                    && Arrays.asList(selectedSizes).contains(size.getSize());

            if (!selected) {
                productSizeRepository.delete(size);
                return true;
            }

            return false;
        });

        // Update existing stock or create new selected sizes
        request.getParameterMap().forEach((key, value) -> {

            if (key.startsWith("sizeStock_")
                    && value.length > 0
                    && !value[0].isBlank()) {

                String sizeName = key.substring("sizeStock_".length());

                int stock = Integer.parseInt(value[0]);

                ProductSize productSize = existingProduct.getSizes()
                        .stream()
                        .filter(size -> size.getSize().equals(sizeName))
                        .findFirst()
                        .orElse(null);

                if (productSize != null) {

                    productSize.setStockQuantity(stock);
                    productSizeRepository.save(productSize);

                } else {

                    boolean selected = selectedSizes != null
                            && Arrays.asList(selectedSizes).contains(sizeName);

                    if (selected) {
                        ProductSize newSize = new ProductSize();

                        newSize.setSize(sizeName);
                        newSize.setStockQuantity(stock);
                        newSize.setProduct(existingProduct);

                        productSizeRepository.save(newSize);
                        existingProduct.getSizes().add(newSize);
                    }
                }
            }
        });

        return "redirect:/admin/products";
    }

    // =========================================
    // DELETE PRODUCT IMAGE
    // =========================================

    @PostMapping("/admin/products/image/delete")
    public String deleteProductImage(
            @RequestParam("imageId") Long imageId,
            @RequestParam("productId") Long productId) throws IOException {

        productService.deleteProductImage(imageId);

        return "redirect:/admin/products/edit?id=" + productId;
    }

    // =========================================
    // DELETE PRODUCT
    // =========================================

    @GetMapping("/admin/products/delete")
    public String deleteProduct(
            @RequestParam("id") Long id) throws IOException{

        productService.deleteProduct(id);

        return "redirect:/admin/products";
    }
}
