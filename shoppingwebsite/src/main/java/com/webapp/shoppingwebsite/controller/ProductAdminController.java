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

    @Autowired
    private ProductsRepository products;


    @GetMapping("/getproductwithid/{productid}")
    @PreAuthorize("hasRole('ADMIN')")
    public Products getproductwithid(@PathVariable("productid") String productid) {
        Optional<Products> result= products.findByProductid(productid);
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
            }


            URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
            return ResponseEntity.created(location)
                    .body(new ApiResponse(true, "Product updated successfully"));

        }else {
            Optional<Products> productOptional = products.findByTitle(productrequest.getTitle());

            productOptional.ifPresent(products1 ->
            {
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

                return ResponseEntity.created(location)
                        .body(new ApiResponse(true, "Product delete successfully"));
            }
        }
        return ResponseEntity.created(location)
                .body(new ApiResponse(false, "Product delete Failed !! for Title : "+productrequest.getTitle()));
    }
}
