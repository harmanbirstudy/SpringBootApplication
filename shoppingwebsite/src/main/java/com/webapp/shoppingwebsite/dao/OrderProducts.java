package com.webapp.shoppingwebsite.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;

@DynamoDBDocument
public class OrderProducts {
    private String title;
    private String price;
    private String imageurl;
    private int quantity;

    @DynamoDBAttribute(attributeName = "quantity")
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }



    @DynamoDBAttribute(attributeName = "title")
    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {

        this.title = title;
    }

    @DynamoDBAttribute(attributeName = "price")
    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {

        this.price = price;
    }

    @DynamoDBAttribute(attributeName = "imageurl")
    public String getImageurl() {

        return imageurl;
    }

    public void setImageurl(String imageurl) {

        this.imageurl = imageurl;
    }


}
