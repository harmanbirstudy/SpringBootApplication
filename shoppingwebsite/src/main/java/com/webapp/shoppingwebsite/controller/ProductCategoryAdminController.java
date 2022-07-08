package com.webapp.shoppingwebsite.controller;

import com.webapp.shoppingwebsite.dao.ProductCategory;
import com.webapp.shoppingwebsite.dao.User;
import com.webapp.shoppingwebsite.exception.ResourceNotFoundException;
import com.webapp.shoppingwebsite.repository.ProductCategoryRepository;
import com.webapp.shoppingwebsite.security.CurrentUser;
import com.webapp.shoppingwebsite.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
@RestController
@RequestMapping("/services/productcategory")
public class ProductCategoryAdminController {
    @Autowired
    private ProductCategoryRepository productcategory;

    @GetMapping("/getlist")
    public List<ProductCategory> getProductCategoryList() {
        List<ProductCategory> result = new ArrayList<ProductCategory>();
        productcategory.findAll(). forEach(result::add);
        result.sort(Comparator.comparing(ProductCategory::getType));
        return result;
    }
}
