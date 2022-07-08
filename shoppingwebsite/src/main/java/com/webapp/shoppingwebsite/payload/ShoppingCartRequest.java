package com.webapp.shoppingwebsite.payload;

public class ShoppingCartRequest {

    public String getCartid() {
        return cartid;
    }

    public void setCartid(String cartid) {

        this.cartid = cartid;
    }


    public String getProductid() {
        return productid;
    }

    public void setProductid(String productid) {
        this.productid = productid;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {

        this.quantity = quantity;
    }

    private String productid;
    private Integer quantity;
    private String cartid;


}
