package com.webapp.shoppingwebsite.controller;

import com.webapp.shoppingwebsite.dao.ProductResponse;
import com.webapp.shoppingwebsite.dao.Products;
import com.webapp.shoppingwebsite.dao.ShoppingCart;
import com.webapp.shoppingwebsite.dao.ShoppingCartResponse;
import com.webapp.shoppingwebsite.repository.ProductsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/services/getproducts")
public class ProductController {

    @Autowired
    private ProductsRepository products;

    @GetMapping("/getlist")
    public List<ProductResponse> getProductist() {
        List<Products> result = new ArrayList<Products>();
        products.findAll().forEach(result::add);
       // result.sort(Comparator.comparing(Products::getTitle));
        return populaterespose(result);
    }

    List<ProductResponse> populaterespose(List<Products> productslist){
        List<ProductResponse> responseList=new ArrayList<>();
        productslist.forEach((product)->{
            ProductResponse  response=new ProductResponse();
            response.setProductid(product.getProductid());
            response.setPrice(product.getPrice());
            response.setImageurl(product.getImageurl());
            response.setCategory(product.getCategory());
            response.setTitle(product.getTitle());
            response.setQuantity(0);
            responseList.add(response);
        });
        responseList.sort(Comparator.comparing(ProductResponse::getTitle));
        return responseList;
    }


}
