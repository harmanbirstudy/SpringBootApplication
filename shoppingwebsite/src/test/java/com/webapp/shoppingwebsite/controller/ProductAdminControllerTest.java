package com.webapp.shoppingwebsite.controller;

import com.webapp.shoppingwebsite.dao.Products;
import com.webapp.shoppingwebsite.exception.BadRequestException;
import com.webapp.shoppingwebsite.payload.ApiResponse;
import com.webapp.shoppingwebsite.payload.ProductRequest;
import com.webapp.shoppingwebsite.repository.ProductsRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductAdminControllerTest {

    @Mock
    private ProductsRepository products;

    @InjectMocks
    private ProductAdminController controller;

    @BeforeEach
    void setUpRequestContext() {
        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(new MockHttpServletRequest("POST", "/services/products/save")));
    }

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getProductWithId_returnsProductWhenFound() {
        Products product = product("p-1", "Laptop");
        when(products.findByProductid("p-1")).thenReturn(Optional.of(product));

        assertThat(controller.getproductwithid("p-1")).isSameAs(product);
    }

    @Test
    void getProductWithId_returnsEmptyProductWhenMissing() {
        when(products.findByProductid("missing")).thenReturn(Optional.empty());

        Products result = controller.getproductwithid("missing");

        assertThat(result).isNotNull();
        assertThat(result.getProductid()).isNull();
        assertThat(result.getTitle()).isNull();
    }

    @Test
    void save_withBlankProductId_createsNewProduct() {
        ProductRequest request = request("", "Laptop");
        when(products.findByTitle("Laptop")).thenReturn(Optional.empty());
        when(products.save(any(Products.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> response = controller.registerProduct(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(((ApiResponse) response.getBody()).getMessage()).isEqualTo("Product registered successfully");

        ArgumentCaptor<Products> captor = ArgumentCaptor.forClass(Products.class);
        verify(products).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Laptop");
        assertThat(captor.getValue().getPrice()).isEqualTo("999");
        assertThat(captor.getValue().getCategory()).isEqualTo("electronics");
        assertThat(captor.getValue().getImageurl()).isEqualTo("http://img/laptop.png");
        verify(products, never()).findByProductid(any());
    }

    @Test
    void save_withBlankProductIdAndDuplicateTitle_throwsBadRequest() {
        when(products.findByTitle("Laptop")).thenReturn(Optional.of(product("p-1", "Laptop")));

        assertThatThrownBy(() -> controller.registerProduct(request("", "Laptop")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Product with same title already exist");

        verify(products, never()).save(any());
    }

    @Test
    void save_withExistingProductId_updatesThatProduct() {
        Products existing = product("p-1", "Old title");
        when(products.findByProductid("p-1")).thenReturn(Optional.of(existing));
        when(products.save(existing)).thenReturn(existing);

        ResponseEntity<?> response = controller.registerProduct(request("p-1", "New title"));

        assertThat(((ApiResponse) response.getBody()).getMessage()).isEqualTo("Product updated successfully");
        assertThat(existing.getTitle()).isEqualTo("New title");
        assertThat(existing.getPrice()).isEqualTo("999");
        verify(products).save(existing);
        verify(products, never()).findByTitle(any());
    }

    @Test
    void save_withUnknownProductId_doesNotSave() {
        when(products.findByProductid("unknown")).thenReturn(Optional.empty());

        controller.registerProduct(request("unknown", "Laptop"));

        verify(products, never()).save(any());
    }

    @Test
    void delete_withExistingProduct_deletesAndReportsSuccess() {
        Products existing = product("p-1", "Laptop");
        when(products.findByProductid("p-1")).thenReturn(Optional.of(existing));

        ResponseEntity<?> response = controller.deleteProduct(request("p-1", "Laptop"));

        verify(products).delete(existing);
        assertThat(((ApiResponse) response.getBody()).isSuccess()).isTrue();
    }

    @Test
    void delete_withUnknownProduct_reportsFailure() {
        when(products.findByProductid("unknown")).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.deleteProduct(request("unknown", "Laptop"));

        verify(products, never()).delete(any());
        ApiResponse body = (ApiResponse) response.getBody();
        assertThat(body.isSuccess()).isFalse();
        assertThat(body.getMessage()).contains("Laptop");
    }

    @Test
    void delete_withNullProductId_reportsFailureWithoutLookup() {
        ResponseEntity<?> response = controller.deleteProduct(request(null, "Laptop"));

        verifyNoInteractions(products);
        assertThat(((ApiResponse) response.getBody()).isSuccess()).isFalse();
    }

    private static ProductRequest request(String productid, String title) {
        ProductRequest request = new ProductRequest();
        request.setProductid(productid);
        request.setTitle(title);
        request.setPrice("999");
        request.setCategory("electronics");
        request.setImageurl("http://img/laptop.png");
        return request;
    }

    private static Products product(String id, String title) {
        Products product = new Products();
        product.setProductid(id);
        product.setTitle(title);
        return product;
    }
}
