package com.webapp.shoppingwebsite.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.*;

import java.util.Date;
import java.util.List;

@DynamoDBTable(tableName = "orders")
public class Orders {
    String userid;
    String orderid;
    Date orderdate;
    String name ;
    String addline1;
    String addline2;
    String city;
    String state;
    String zipcode;
    List<OrderProducts> products;

    @DynamoDBHashKey(attributeName = "orderid")
    @DynamoDBAutoGeneratedKey
    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    @DynamoDBIndexHashKey(attributeName = "userid", globalSecondaryIndexName = "useridx_id")
    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    @DynamoDBAttribute(attributeName = "orderdate")
    @DynamoDBAutoGeneratedTimestamp(strategy= DynamoDBAutoGenerateStrategy.CREATE)
    public Date getOrderdate() {
        return orderdate;
    }

    public void setOrderdate(Date orderdate) {
        this.orderdate = orderdate;
    }
    @DynamoDBAttribute(attributeName = "products")
    public List<OrderProducts> getProducts() {
        return products;
    }

    public void setProducts(List<OrderProducts> products) {
        this.products = products;
    }

    @DynamoDBAttribute(attributeName = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @DynamoDBAttribute(attributeName = "addline1")
    public String getAddline1() {
        return addline1;
    }

    public void setAddline1(String addline1) {
        this.addline1 = addline1;
    }

    @DynamoDBAttribute(attributeName = "addline2")
    public String getAddline2() {
        return addline2;
    }

    public void setAddline2(String addline2) {
        this.addline2 = addline2;
    }

    @DynamoDBAttribute(attributeName = "city")
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @DynamoDBAttribute(attributeName = "state")
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @DynamoDBAttribute(attributeName = "zipcode")
    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }





}