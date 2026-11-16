package com.fashionkart.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fashionkart.entity.Product;
import com.fashionkart.service.ProductService;

@Controller
public class SearchController {

    private ProductService productService;

    public SearchController(ProductService productService) {
        this.productService = productService;
    }


    @GetMapping("/search")
    public String searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model) {


        // Empty search

        if (keyword == null || keyword.isBlank()) {

            model.addAttribute("products", List.of());

            model.addAttribute("keyword", "");

            model.addAttribute("currentPage", 0);

            model.addAttribute("totalPages", 1);

            return "customer/search";

        }


        // Search products

        List<Product> allProducts =
                productService.searchProductsOverall(keyword.trim());


        // Pagination

        int pageSize = 8;

        int totalProducts = allProducts.size();

        int totalPages =
                (int) Math.ceil((double) totalProducts / pageSize);


        if (totalPages == 0) {
            totalPages = 1;
        }


        if (page < 0) {
            page = 0;
        }


        if (page >= totalPages) {
            page = totalPages - 1;
        }


        int start = page * pageSize;

        int end =
                Math.min(start + pageSize, totalProducts);


        List<Product> products =
                allProducts.subList(start, end);


        // Send data to page

        model.addAttribute("products", products);

        model.addAttribute("keyword", keyword);

        model.addAttribute("currentPage", page);

        model.addAttribute("totalPages", totalPages);


        return "customer/search";
    }
}