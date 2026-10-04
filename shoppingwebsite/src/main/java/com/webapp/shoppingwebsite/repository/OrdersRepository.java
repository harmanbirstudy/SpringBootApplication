package com.webapp.shoppingwebsite.repository;

import com.webapp.shoppingwebsite.dao.Orders;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface OrdersRepository  extends CrudRepository<Orders, String> {

    Optional<Orders> findByOrderid(String orderid);
    List<Orders> findByUserid(String userid);
}
