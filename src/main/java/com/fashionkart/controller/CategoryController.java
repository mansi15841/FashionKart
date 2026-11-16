package com.fashionkart.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fashionkart.entity.Product;
import com.fashionkart.repository.ProductRepository;

@Controller
public class CategoryController {

    private ProductRepository productRepository;

    public CategoryController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }


    // =========================================================
    // WOMEN
    // =========================================================

    @GetMapping("/women")
    public String women(
            @RequestParam(required = false) String subCategory,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        List<Product> allProducts;

        if (subCategory == null || subCategory.isBlank()) {

            allProducts =
                    productRepository.findByCategoryIgnoreCase("Women");

        } else {

            allProducts =
                    productRepository.findByCategoryAndSubCategoryIgnoreCase(
                            "Women",
                            subCategory
                    );
        }


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


        List<String> subCategories =
                productRepository.findDistinctSubCategoriesByCategory("Women");


        model.addAttribute("products", products);

        model.addAttribute("category", "Women");

        model.addAttribute("selectedSubCategory", subCategory);

        model.addAttribute("subCategories", subCategories);

        model.addAttribute("currentPage", page);

        model.addAttribute("totalPages", totalPages);


        return "customer/category";
    }



    // =========================================================
    // MEN
    // =========================================================

    @GetMapping("/men")
    public String men(
            @RequestParam(required = false) String subCategory,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        List<Product> allProducts;

        if (subCategory == null || subCategory.isBlank()) {

            allProducts =
                    productRepository.findByCategoryIgnoreCase("Men");

        } else {

            allProducts =
                    productRepository.findByCategoryAndSubCategoryIgnoreCase(
                            "Men",
                            subCategory
                    );
        }


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


        List<String> subCategories =
                productRepository.findDistinctSubCategoriesByCategory("Men");


        model.addAttribute("products", products);

        model.addAttribute("category", "Men");

        model.addAttribute("selectedSubCategory", subCategory);

        model.addAttribute("subCategories", subCategories);

        model.addAttribute("currentPage", page);

        model.addAttribute("totalPages", totalPages);


        return "customer/category";
    }



    // =========================================================
    // KIDS
    // =========================================================

    @GetMapping("/kids")
    public String kids(
            @RequestParam(required = false) String subCategory,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        List<Product> allProducts;

        if (subCategory == null || subCategory.isBlank()) {

            allProducts =
                    productRepository.findByCategoryIgnoreCase("Kids");

        } else {

            allProducts =
                    productRepository.findByCategoryAndSubCategoryIgnoreCase(
                            "Kids",
                            subCategory
                    );
        }


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


        List<String> subCategories =
                productRepository.findDistinctSubCategoriesByCategory("Kids");


        model.addAttribute("products", products);

        model.addAttribute("category", "Kids");

        model.addAttribute("selectedSubCategory", subCategory);

        model.addAttribute("subCategories", subCategories);

        model.addAttribute("currentPage", page);

        model.addAttribute("totalPages", totalPages);


        return "customer/category";
    }

}