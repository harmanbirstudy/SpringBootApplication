package com.webapp.shoppingwebsite.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.*;

@DynamoDBTable(tableName = "products")
public class Products {

    @DynamoDBIndexHashKey(attributeName = "productid", globalSecondaryIndexName = "product_id")
    @DynamoDBAutoGeneratedKey
    public String getProductid() {
        return productid;
    }

    public void setProductid(String productid) {
        this.productid = productid;
    }
    @DynamoDBHashKey(attributeName = "title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    @DynamoDBAttribute
    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
    @DynamoDBAttribute
    public String getImageurl() {
        return imageurl;
    }

    @DynamoDBAttribute
    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    private String productid;
    private String title;
    private String price;
    private String imageurl;
    private String category;
}
