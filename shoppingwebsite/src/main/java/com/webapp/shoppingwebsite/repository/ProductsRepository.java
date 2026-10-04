package com.webapp.shoppingwebsite.repository;

import com.webapp.shoppingwebsite.dao.Products;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ProductsRepository extends CrudRepository<Products, String> {

    Optional<Products> findByProductid(String productid);
    Optional<Products> findByTitle(String title);
}