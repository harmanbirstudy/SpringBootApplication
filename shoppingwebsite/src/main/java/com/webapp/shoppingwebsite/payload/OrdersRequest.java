package com.webapp.shoppingwebsite.payload;

public class OrdersRequest {

    public String getCartid() {
        return cartid;
    }

    public void setCartid(String cartid) {
        this.cartid = cartid;
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
    String cartid;
    String name ;
    String addline1;
    String addline2;
    String city;
    String state;
    String zipcode;
}
