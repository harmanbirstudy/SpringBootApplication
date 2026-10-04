package com.webapp.shoppingwebsite.dao;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders", indexes = @Index(name = "orders_userid_idx", columnList = "userid"))
public class Orders {
    @Id
    @Column(name = "orderid")
    String orderid;

    @Column(name = "userid")
    String userid;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "orderdate", updatable = false)
    Date orderdate;

    @Column(name = "name")
    String name ;

    @Column(name = "addline1")
    String addline1;

    @Column(name = "addline2")
    String addline2;

    @Column(name = "city")
    String city;

    @Column(name = "state")
    String state;

    @Column(name = "zipcode")
    String zipcode;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_products", joinColumns = @JoinColumn(name = "orderid"))
    @OrderColumn(name = "position")
    List<OrderProducts> products = new ArrayList<>();

    @PrePersist
    void generateId() {
        if (orderid == null) {
            orderid = UUID.randomUUID().toString();
        }
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public Date getOrderdate() {
        return orderdate;
    }

    public void setOrderdate(Date orderdate) {
        this.orderdate = orderdate;
    }

    public List<OrderProducts> getProducts() {
        return products;
    }

    public void setProducts(List<OrderProducts> products) {
        this.products = products;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddline1() {
        return addline1;
    }

    public void setAddline1(String addline1) {
        this.addline1 = addline1;
    }

    public String getAddline2() {
        return addline2;
    }

    public void setAddline2(String addline2) {
        this.addline2 = addline2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

}
