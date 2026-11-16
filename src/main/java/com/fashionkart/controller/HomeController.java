package com.fashionkart.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fashionkart.entity.Product;
import com.fashionkart.entity.User;
import com.fashionkart.service.ProductService;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
	
	private ProductService productService;
	
	public HomeController(ProductService productService) {
		// TODO Auto-generated constructor stub
		this.productService = productService;
	}

	//customer-pages
	
	@GetMapping("/")
	public String home(HttpSession httpSession, Model model) {
		User user = (User) httpSession.getAttribute("loggedInUser");
		
		if(user!=null) {
			model.addAttribute("loggedInUser", user);
		}
		
		model.addAttribute("products", productService.getAllProducts().stream().limit(4).toList());
		
		return "customer/index";
	}
	
	@GetMapping("/add-address")
	public String addAddress() {
		return "customer/add-address";
	}
	
	@GetMapping("/products")
	public String products(
	        @RequestParam(defaultValue = "featured") String sort,
	        @RequestParam(defaultValue = "0") int page,
	        Model model) {

	    List<Product> allProducts =
	            productService.getProductsSortedBy(sort);

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
	    int end = Math.min(start + pageSize, totalProducts);

	    List<Product> products =
	            allProducts.subList(start, end);

	    model.addAttribute("products", products);
	    model.addAttribute("sort", sort);
	    model.addAttribute("currentPage", page);
	    model.addAttribute("totalPages", totalPages);

	    return "customer/products";
	}
	
	@GetMapping("/sale")
	public String saleProducts(
	        @RequestParam(required = false) String category,
	        @RequestParam(defaultValue = "0") int page,
	        Model model) {

	    List<Product> allProducts;

	    // Get sale products
	    if (category == null || category.isBlank()) {

	        allProducts = productService.getSaleProducts();

	    } else {

	        allProducts = productService.getSaleProductsByCategory(category);

	    }


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


	    // Send data to Sale page

	    model.addAttribute("products", products);

	    model.addAttribute("category", category);

	    model.addAttribute("currentPage", page);

	    model.addAttribute("totalPages", totalPages);

	    model.addAttribute("sale", true);


	    return "customer/sale";
	}
	
	
	@GetMapping("/product-details")
	public String productDetails(@RequestParam("id") Long id, Model model) {
		Product product = productService.getProductById(id);
		
		model.addAttribute("product",product);
		return "customer/product-details";
	}
	
}

