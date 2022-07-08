package com.webapp.shoppingwebsite.repository;

import com.webapp.shoppingwebsite.dao.ProductCategory;
import com.webapp.shoppingwebsite.dao.User;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

@EnableScan
public interface ProductCategoryRepository  extends CrudRepository<ProductCategory, String> {

    Optional<ProductCategory> findByType(String type);
}
