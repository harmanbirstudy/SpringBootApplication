package com.webapp.shoppingwebsite.dao;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Products {

    @PrePersist
    void generateId() {
        if (productid == null) {
            productid = UUID.randomUUID().toString();
        }
    }

    public String getProductid() {
        return productid;
    }

    public void setProductid(String productid) {
        this.productid = productid;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Id
    @Column(name = "productid")
    private String productid;

    @Column(name = "title", unique = true, nullable = false)
    private String title;

    @Column(name = "price")
    private String price;

    @Column(name = "imageurl")
    private String imageurl;

    @Column(name = "category")
    private String category;
}
