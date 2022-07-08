package com.webapp.shoppingwebsite.repository;

import com.webapp.shoppingwebsite.dao.User;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@EnableScan
public interface UserRepository extends CrudRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUserid(String Id);

}
