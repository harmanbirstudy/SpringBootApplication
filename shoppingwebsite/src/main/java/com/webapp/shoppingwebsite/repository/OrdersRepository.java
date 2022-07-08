package com.webapp.shoppingwebsite.repository;

import com.webapp.shoppingwebsite.dao.Orders;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@EnableScan
public interface OrdersRepository  extends CrudRepository<Orders, String> {

    Optional<Orders> findByOrderid(String orderid);
    Optional<List<Orders>> findByUserid(String userid);
}
