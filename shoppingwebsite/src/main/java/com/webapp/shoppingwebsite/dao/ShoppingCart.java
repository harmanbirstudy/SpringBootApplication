package com.webapp.shoppingwebsite.dao;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "shoppingcart")
public class ShoppingCart {

    @PrePersist
    void generateId() {
        if (cartid == null) {
            cartid = UUID.randomUUID().toString();
        }
    }

    public String getCartid() {
        return cartid;
    }

    public void setCartid(String cartid) {
        this.cartid = cartid;
    }

    public Date getCreateddate() {
        return createddate;
    }

    public void setCreateddate(Date createddate) {
        this.createddate = createddate;
    }

    public Date getModifieddate() {
        return modifieddate;
    }

    public void setModifieddate(Date modifieddate) {
        this.modifieddate = modifieddate;
    }

    public Map<String, Integer> getItems() {
        return items;
    }

    public void setItems(Map<String, Integer> items) {
        this.items = items;
    }

    @Id
    @Column(name = "cartid")
    private String cartid;

    // productid -> quantity
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "shoppingcart_items", joinColumns = @JoinColumn(name = "cartid"))
    @MapKeyColumn(name = "productid")
    @Column(name = "quantity")
    private Map<String, Integer> items = new HashMap<>();

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "createddate", updatable = false)
    private Date createddate;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modifieddate")
    private Date modifieddate;

}
