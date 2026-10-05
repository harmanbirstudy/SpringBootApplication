package com.webapp.shoppingwebsite.controller;

import com.webapp.shoppingwebsite.dao.*;
import com.webapp.shoppingwebsite.payload.ApiResponse;
import com.webapp.shoppingwebsite.payload.OrderIdResponse;
import com.webapp.shoppingwebsite.payload.OrdersRequest;
import com.webapp.shoppingwebsite.payload.ProductRequest;
import com.webapp.shoppingwebsite.repository.OrdersRepository;
import com.webapp.shoppingwebsite.repository.ProductsRepository;
import com.webapp.shoppingwebsite.repository.ShoppingCartRepository;
import com.webapp.shoppingwebsite.repository.UserRepository;
import com.webapp.shoppingwebsite.security.CurrentUser;
import com.webapp.shoppingwebsite.security.UserPrincipal;
import net.minidev.json.JSONObject;
import org.apache.tomcat.util.json.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/services/orders")
public class OrderAdminController implements SecuredRestController {

    private static final Logger logger = LoggerFactory.getLogger(OrderAdminController.class);

    @Autowired
    private OrdersRepository ordersrepo;

    @Autowired
    private ShoppingCartRepository shoppingcartrepo;

    @Autowired
    private ProductsRepository products;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/createorder")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public OrderIdResponse createOrder(@Valid @RequestBody OrdersRequest ordersrequest, @CurrentUser UserPrincipal userPrincipal) {
        ShoppingCart cart;
        Orders order =new Orders();
        List<OrderProducts> orderproductslist=new ArrayList<>();
        Products product ;
        logger.info("Creating order for userid: {} from cartid: {}", userPrincipal.getUserid(), ordersrequest.getCartid());
        order.setUserid(userPrincipal.getUserid());
        order.setName(ordersrequest.getName());
        order.setAddline1(ordersrequest.getAddline1());
        order.setAddline2(ordersrequest.getAddline2());
        order.setCity(ordersrequest.getCity());
        order.setState(ordersrequest.getState());
        order.setZipcode(ordersrequest.getZipcode());
        if (!ordersrequest.getCartid().isBlank()) {
            Optional<ShoppingCart> cartOptional = shoppingcartrepo.findByCartid(ordersrequest.getCartid());
            if (cartOptional.isPresent()) {

                cart = cartOptional.get();

                Map<String, Integer> itemsMap = cart.getItems();
                for (Iterator<Map.Entry<String, Integer>> iterator = itemsMap.entrySet().iterator(); iterator.hasNext(); ) {
                    Map.Entry<String, Integer> next = iterator.next();
                    Optional<Products> productOptional = products.findByProductid(next.getKey());
                    if(productOptional.isPresent()) {
                        product = productOptional.get();
                        OrderProducts orprod=new OrderProducts();
                        orprod.setTitle(product.getTitle());
                        orprod.setPrice(product.getPrice());
                        orprod.setQuantity(next.getValue());
                        orprod.setImageurl(product.getImageurl());
                        orderproductslist.add(orprod);
                       // Products result = products.save(product);
                    } else {
                        logger.warn("Product {} in cart {} not found, skipping it in the order", next.getKey(), ordersrequest.getCartid());
                    }

                }
             order.setProducts(orderproductslist);
            } else {
                logger.warn("Cart not found for cartid: {}, creating order without products", ordersrequest.getCartid());
            }

        }
        Orders result=ordersrepo.save(order);
        logger.info("Order {} created for userid: {} with {} product(s)", result.getOrderid(), userPrincipal.getUserid(), orderproductslist.size());
        OrderIdResponse resp=new OrderIdResponse();
        resp.setOrderid(result.getOrderid());
        return  resp;

    }
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/getorderdetails/{orderid}")
    public Orders getOrderDetails(@PathVariable("orderid") String orderid){
        logger.debug("Fetching order details for orderid: {}", orderid);
        if (!orderid.isBlank()) {
            Optional<Orders> cartOptional = ordersrepo.findByOrderid(orderid);
            if (cartOptional.isPresent()) {
                Orders result = cartOptional.get();
                return result;
            }
        }
        logger.warn("Order not found for orderid: {}", orderid);
        return null;

    }
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/getalluserorders")
    public List<OrderUserIdResponse> getOrdersByUser( @CurrentUser UserPrincipal userPrincipal)  {
        List<Orders> orders = ordersrepo.findByUserid(userPrincipal.getUserid());
        logger.debug("Found {} order(s) for userid: {}", orders.size(), userPrincipal.getUserid());
        List<OrderUserIdResponse> result = new ArrayList<>();
        //Initializing the date formatter
       // SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss", Locale.ENGLISH);
        if(orders.size()>0) {
            for (Orders order : orders) {
                OrderUserIdResponse newresp = new OrderUserIdResponse();
                newresp.setOrderid(order.getOrderid());
              //  newresp.setOrderdate(formatter.parse(formatter.format(order.getOrderdate())));
                newresp.setOrderdate(order.getOrderdate());
                result.add(newresp);
            }

            result.sort(Comparator.comparing(OrderUserIdResponse::getOrderdate));
            return result;
        }
        return null;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getallorders")
    public List<AllOrdersResponse> getAllOrders()  {
        List<AllOrdersResponse> result = new ArrayList<AllOrdersResponse>();
        List<Orders> allorders = new ArrayList<Orders>();
        ordersrepo.findAll().forEach(allorders::add);
        logger.debug("Admin fetching all orders, total: {}", allorders.size());
        if(allorders.size()>0) {
            for (Orders order : allorders) {
                Optional<User> user = userRepository.findByUserid(order.getUserid());
                if (user.isPresent()) {
                    AllOrdersResponse userorder = new AllOrdersResponse();
                    userorder.setName(user.get().getName());
                    userorder.setOrderid(order.getOrderid());
                    userorder.setOrderdate(order.getOrderdate());
                    result.add(userorder);
                } else {
                    logger.warn("User {} not found for order {}", order.getUserid(), order.getOrderid());
                }
            }
            result.sort(Comparator.comparing(AllOrdersResponse::getOrderdate));
            return result;
        }
        return null;
    }


}