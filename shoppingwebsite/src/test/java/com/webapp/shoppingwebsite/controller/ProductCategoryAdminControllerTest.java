package com.webapp.shoppingwebsite.controller;

import com.webapp.shoppingwebsite.dao.ProductCategory;
import com.webapp.shoppingwebsite.repository.ProductCategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductCategoryAdminControllerTest {

    @Mock
    private ProductCategoryRepository productcategory;

    @InjectMocks
    private ProductCategoryAdminController controller;

    @Test
    void getProductCategoryList_returnsCategoriesSortedByType() {
        when(productcategory.findAll()).thenReturn(List.of(category("toys"), category("books"), category("garden")));

        assertThat(controller.getProductCategoryList())
                .extracting(ProductCategory::getType)
                .containsExactly("books", "garden", "toys");
    }

    @Test
    void getProductCategoryList_returnsEmptyListWhenNoCategories() {
        when(productcategory.findAll()).thenReturn(List.of());

        assertThat(controller.getProductCategoryList()).isEmpty();
    }

    private static ProductCategory category(String type) {
        ProductCategory category = new ProductCategory();
        category.setType(type);
        category.setName(type.toUpperCase());
        return category;
    }
}
