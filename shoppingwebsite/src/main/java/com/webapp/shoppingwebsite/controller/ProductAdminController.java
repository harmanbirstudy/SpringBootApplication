package com.webapp.shoppingwebsite.controller;

import com.webapp.shoppingwebsite.dao.AuthProvider;
import com.webapp.shoppingwebsite.dao.Products;
import com.webapp.shoppingwebsite.dao.User;
import com.webapp.shoppingwebsite.exception.BadRequestException;
import com.webapp.shoppingwebsite.exception.OAuth2AuthenticationProcessingException;
import com.webapp.shoppingwebsite.payload.ApiResponse;
import com.webapp.shoppingwebsite.payload.ProductRequest;
import com.webapp.shoppingwebsite.payload.SignUpRequest;
import com.webapp.shoppingwebsite.repository.ProductsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/services/products")
public class ProductAdminController implements SecuredRestController{

    private static final Logger logger = LoggerFactory.getLogger(ProductAdminController.class);

    @Autowired
    private ProductsRepository products;


    @GetMapping("/getproductwithid/{productid}")
    @PreAuthorize("hasRole('ADMIN')")
    public Products getproductwithid(@PathVariable("productid") String productid) {
        logger.debug("Fetching product with productid: {}", productid);
        Optional<Products> result= products.findByProductid(productid);
        if (result.isEmpty()) {
            logger.warn("Product not found for productid: {}", productid);
        }
        Products product = result.orElse(new Products());
        return product;
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerProduct(@Valid @RequestBody ProductRequest productrequest) {
        //String title=productrequest.getTitle().toLowerCase(Locale.ROOT);
        Products product ;
        if(!productrequest.getProductid().isBlank()){
            Optional<Products> productOptional = products.findByProductid(productrequest.getProductid());
            if(productOptional.isPresent()) {
                product = productOptional.get();
                product.setTitle(productrequest.getTitle());
                product.setPrice(productrequest.getPrice());
                product.setCategory(productrequest.getCategory());
                product.setImageurl(productrequest.getImageurl());
                Products result = products.save(product);
                logger.info("Product updated, productid: {}, title: {}", result.getProductid(), result.getTitle());
            } else {
                logger.warn("Product update skipped, productid not found: {}", productrequest.getProductid());
            }


            URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
            return ResponseEntity.created(location)
                    .body(new ApiResponse(true, "Product updated successfully"));

        }else {
            Optional<Products> productOptional = products.findByTitle(productrequest.getTitle());

            productOptional.ifPresent(products1 ->
            {
                logger.warn("Product create rejected, title already exists: {}", productrequest.getTitle());
                throw new BadRequestException("Product with same title already exist");
            })
            ;


            // Creating user's account
             product = new Products();
            product.setTitle(productrequest.getTitle());
            product.setPrice(productrequest.getPrice());
            product.setCategory(productrequest.getCategory());
            product.setImageurl(productrequest.getImageurl());

            Products result = products.save(product);
            logger.info("Product created, productid: {}, title: {}", result.getProductid(), result.getTitle());

            URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();

            return ResponseEntity.created(location)
                    .body(new ApiResponse(true, "Product registered successfully"));
        }
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?>  deleteProduct(@Valid @RequestBody ProductRequest productrequest) {
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        Products product;
        if (productrequest.getProductid() != null) {
            Optional<Products> productOptional = products.findByProductid(productrequest.getProductid());
            if (productOptional.isPresent()) {
                product = productOptional.get();
                products.delete(product);
                logger.info("Product deleted, productid: {}, title: {}", product.getProductid(), product.getTitle());

                return ResponseEntity.created(location)
                        .body(new ApiResponse(true, "Product delete successfully"));
            }
        }
        logger.warn("Product delete failed, productid: {}, title: {}", productrequest.getProductid(), productrequest.getTitle());
        return ResponseEntity.created(location)
                .body(new ApiResponse(false, "Product delete Failed !! for Title : "+productrequest.getTitle()));
    }
}
