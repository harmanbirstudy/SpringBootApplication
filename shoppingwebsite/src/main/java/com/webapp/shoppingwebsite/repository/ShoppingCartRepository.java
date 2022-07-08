package com.webapp.shoppingwebsite.repository;

import com.webapp.shoppingwebsite.dao.ShoppingCart;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

@EnableScan
public interface ShoppingCartRepository  extends CrudRepository<ShoppingCart, String> {
    Optional<ShoppingCart> findByCartid(String cartid);
}
