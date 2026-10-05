package com.webapp.shoppingwebsite.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class OrderProducts {
    @Column(name = "title")
    private String title;

    @Column(name = "price")
    private String price;

    @Column(name = "imageurl")
    private String imageurl;

    @Column(name = "quantity")
    private int quantity;

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }



    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {

        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {

        this.price = price;
    }

    public String getImageurl() {

        return imageurl;
    }

    public void setImageurl(String imageurl) {

        this.imageurl = imageurl;
    }


}
