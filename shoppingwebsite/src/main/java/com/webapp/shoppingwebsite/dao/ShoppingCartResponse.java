package com.webapp.shoppingwebsite.dao;

import java.util.List;

public class ShoppingCartResponse {
    String cartid;

    public String getCartid() {
        return cartid;
    }

    public void setCartid(String cartid) {
        this.cartid = cartid;
    }

    public List<ProductResponse> getProducts() {
        return products;
    }

    public void setProducts(List<ProductResponse> products) {
        this.products = products;
    }

    List<ProductResponse> products;



}
