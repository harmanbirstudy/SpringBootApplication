package com.webapp.shoppingwebsite.controller;

import com.webapp.shoppingwebsite.dao.ProductResponse;
import com.webapp.shoppingwebsite.dao.Products;
import com.webapp.shoppingwebsite.dao.ShoppingCart;
//import com.webapp.shoppingwebsite.dao.ShoppingCartItems;
import com.webapp.shoppingwebsite.dao.ShoppingCartResponse;
import com.webapp.shoppingwebsite.payload.ShoppingCartRequest;
import com.webapp.shoppingwebsite.repository.ProductsRepository;
import com.webapp.shoppingwebsite.repository.ShoppingCartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/services/shoppingcart")
public class ShoppingCartController {

    private static final Logger logger = LoggerFactory.getLogger(ShoppingCartController.class);

    @Autowired
    private ShoppingCartRepository shoppingcartrepo;

    @Autowired
    private ProductsRepository products;

    @PostMapping("/createorupdate")
    ShoppingCartResponse createOrUpdateCart(@Valid @RequestBody ShoppingCartRequest cartreuquest) {
        logger.debug("Cart update request, cartid: {}, productid: {}, quantity: {}",
                cartreuquest.getCartid(), cartreuquest.getProductid(), cartreuquest.getQuantity());

        if (!cartreuquest.getCartid().isBlank()) {
            Optional<ShoppingCart> cartOptional = shoppingcartrepo.findByCartid(cartreuquest.getCartid());
            ShoppingCart cart;

            if (cartOptional.isPresent()) {
                cart = cartOptional.get();
                Map<String, Integer> itemsMap = cart.getItems();
                for (Iterator<Map.Entry<String, Integer>> iterator = itemsMap.entrySet().iterator(); iterator.hasNext(); ) {
                    Map.Entry<String, Integer> next = iterator.next();
                    if (next.getKey().equalsIgnoreCase(cartreuquest.getProductid())) {
                        if (cartreuquest.getQuantity() <= 0) {
                            iterator.remove();
                            logger.info("Removed productid: {} from cartid: {}", cartreuquest.getProductid(), cart.getCartid());
                        } else {
                            next.setValue(cartreuquest.getQuantity());
                            logger.info("Updated productid: {} quantity to {} in cartid: {}", cartreuquest.getProductid(), cartreuquest.getQuantity(), cart.getCartid());
                        }
                        shoppingcartrepo.save(cart);
                        return populaterespose(cart);

                    }

                }
                itemsMap.put(cartreuquest.getProductid(), cartreuquest.getQuantity());
                shoppingcartrepo.save(cart);
                logger.info("Added productid: {} (quantity {}) to cartid: {}", cartreuquest.getProductid(), cartreuquest.getQuantity(), cart.getCartid());

                return populaterespose(cart);
            }
            logger.warn("Cart not found for cartid: {}", cartreuquest.getCartid());
        } else {
            ShoppingCart cart = new ShoppingCart();
            Map<String, Integer> itemsMap = new HashMap<>();
            itemsMap.put(cartreuquest.getProductid(), cartreuquest.getQuantity());
            cart.setItems(itemsMap);
            shoppingcartrepo.save(cart);
            logger.info("Created new cartid: {} with productid: {}", cart.getCartid(), cartreuquest.getProductid());
            return populaterespose(cart);
        }
        return null;
    }

    ShoppingCartResponse populaterespose(ShoppingCart cart) {
        ShoppingCartResponse response = new ShoppingCartResponse();
        response.setCartid(cart.getCartid());
        List<ProductResponse> productlist = new ArrayList<>();

        for (Iterator<Map.Entry<String, Integer>> iterator = cart.getItems().entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Integer> items = iterator.next();

            Optional<Products> productOptional = products.findByProductid(items.getKey());
            if (productOptional.isPresent()) {
                ProductResponse productitem = new ProductResponse();
                Products products = productOptional.get();
                ;
                productitem.setProductid(products.getProductid());
                productitem.setCategory(products.getCategory());
                productitem.setTitle(products.getTitle());
                productitem.setPrice(products.getPrice());
                productitem.setImageurl(products.getImageurl());
                productitem.setQuantity(items.getValue());
                productlist.add(productitem);
            } else {
                logger.warn("Product {} in cartid: {} no longer exists", items.getKey(), cart.getCartid());
            }

        }
        response.setProducts(productlist);
        return response;
    }

    @GetMapping("/getcart/{cartid}")
    ShoppingCartResponse getCart(@PathVariable("cartid") String cartid) {
        if (!cartid.isBlank()) {
            Optional<ShoppingCart> cartOptional = shoppingcartrepo.findByCartid(cartid);
            if (cartOptional.isPresent()) {
                ShoppingCart cart = cartOptional.get();
                return populaterespose(cart);
            }
        }
        logger.warn("Cart not found for cartid: {}", cartid);
        return null;
    }

    @GetMapping("/clearcart/{cartid}")
    ShoppingCartResponse  clearCart(@PathVariable("cartid") String cartid) {

        Optional<ShoppingCart> cartOptional = shoppingcartrepo.findByCartid(cartid);
        ShoppingCart cart;
        if (cartOptional.isPresent()) {
            cart = cartOptional.get();
            Map<String, Integer> itemsMap = cart.getItems();
            for (Iterator<Map.Entry<String, Integer>> iterator = itemsMap.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, Integer> next = iterator.next();
                iterator.remove();

            }
            shoppingcartrepo.save(cart);
            logger.info("Cleared cartid: {}", cartid);
            return populaterespose(cart);
        }
        logger.warn("Clear cart failed, cart not found for cartid: {}", cartid);
        return null;
    }

}
