package com.webapp.shoppingwebsite.controller;

import com.webapp.shoppingwebsite.dao.ProductResponse;
import com.webapp.shoppingwebsite.dao.Products;
import com.webapp.shoppingwebsite.repository.ProductsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductsRepository products;

    @InjectMocks
    private ProductController controller;

    @Test
    void getProductList_mapsProductsSortedByTitleWithZeroQuantity() {
        when(products.findAll()).thenReturn(List.of(
                product("p-2", "Phone", "499"),
                product("p-1", "Camera", "250"),
                product("p-3", "Watch", "199")));

        List<ProductResponse> result = controller.getProductist();

        assertThat(result).extracting(ProductResponse::getTitle).containsExactly("Camera", "Phone", "Watch");
        assertThat(result).extracting(ProductResponse::getQuantity).containsOnly(0);
        ProductResponse camera = result.get(0);
        assertThat(camera.getProductid()).isEqualTo("p-1");
        assertThat(camera.getPrice()).isEqualTo("250");
        assertThat(camera.getCategory()).isEqualTo("electronics");
        assertThat(camera.getImageurl()).isEqualTo("http://img/p-1.png");
    }

    @Test
    void getProductList_returnsEmptyListWhenNoProducts() {
        when(products.findAll()).thenReturn(List.of());

        assertThat(controller.getProductist()).isEmpty();
    }

    private static Products product(String id, String title, String price) {
        Products product = new Products();
        product.setProductid(id);
        product.setTitle(title);
        product.setPrice(price);
        product.setCategory("electronics");
        product.setImageurl("http://img/" + id + ".png");
        return product;
    }
}
