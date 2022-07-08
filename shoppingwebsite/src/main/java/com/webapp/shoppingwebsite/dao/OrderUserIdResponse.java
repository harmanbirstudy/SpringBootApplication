package com.webapp.shoppingwebsite.dao;

import java.util.Date;

public class OrderUserIdResponse {
    public Date getOrderdate() {
        return orderdate;
    }

    public void setOrderdate(Date orderdate) {
        this.orderdate = orderdate;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    String orderid;
    Date orderdate;
}
