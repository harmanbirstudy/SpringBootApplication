package com.webapp.shoppingwebsite.controller;

import com.webapp.shoppingwebsite.dao.ProductResponse;
import com.webapp.shoppingwebsite.dao.Products;
import com.webapp.shoppingwebsite.dao.ShoppingCart;
import com.webapp.shoppingwebsite.dao.ShoppingCartResponse;
import com.webapp.shoppingwebsite.payload.ShoppingCartRequest;
import com.webapp.shoppingwebsite.repository.ProductsRepository;
import com.webapp.shoppingwebsite.repository.ShoppingCartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingCartControllerTest {

    @Mock
    private ShoppingCartRepository shoppingcartrepo;

    @Mock
    private ProductsRepository products;

    @InjectMocks
    private ShoppingCartController controller;

    @Test
    void createOrUpdate_withBlankCartId_createsNewCartWithItem() {
        stubProduct("p-1", "Laptop");

        ShoppingCartResponse response = controller.createOrUpdateCart(request("", "p-1", 2));

        ArgumentCaptor<ShoppingCart> captor = ArgumentCaptor.forClass(ShoppingCart.class);
        verify(shoppingcartrepo).save(captor.capture());
        assertThat(captor.getValue().getItems()).containsExactly(Map.entry("p-1", 2));
        verify(shoppingcartrepo, never()).findByCartid(anyString());

        assertThat(response.getProducts()).singleElement().satisfies(item -> {
            assertThat(item.getProductid()).isEqualTo("p-1");
            assertThat(item.getTitle()).isEqualTo("Laptop");
            assertThat(item.getQuantity()).isEqualTo(2);
        });
    }

    @Test
    void createOrUpdate_addsNewProductToExistingCart() {
        ShoppingCart cart = cart("c-1", Map.of("p-1", 1));
        when(shoppingcartrepo.findByCartid("c-1")).thenReturn(Optional.of(cart));
        stubProduct("p-1", "Laptop");
        stubProduct("p-2", "Mouse");

        ShoppingCartResponse response = controller.createOrUpdateCart(request("c-1", "p-2", 3));

        assertThat(cart.getItems()).containsOnly(Map.entry("p-1", 1), Map.entry("p-2", 3));
        verify(shoppingcartrepo).save(cart);
        assertThat(response.getCartid()).isEqualTo("c-1");
        assertThat(response.getProducts()).extracting(ProductResponse::getProductid).containsExactlyInAnyOrder("p-1", "p-2");
    }

    @Test
    void createOrUpdate_updatesQuantityOfProductAlreadyInCart() {
        ShoppingCart cart = cart("c-1", Map.of("p-1", 1));
        when(shoppingcartrepo.findByCartid("c-1")).thenReturn(Optional.of(cart));
        stubProduct("p-1", "Laptop");

        ShoppingCartResponse response = controller.createOrUpdateCart(request("c-1", "p-1", 5));

        assertThat(cart.getItems()).containsExactly(Map.entry("p-1", 5));
        verify(shoppingcartrepo).save(cart);
        assertThat(response.getProducts()).singleElement().extracting(ProductResponse::getQuantity).isEqualTo(5);
    }

    @Test
    void createOrUpdate_matchesProductIdIgnoringCase() {
        ShoppingCart cart = cart("c-1", Map.of("P-1", 1));
        when(shoppingcartrepo.findByCartid("c-1")).thenReturn(Optional.of(cart));

        controller.createOrUpdateCart(request("c-1", "p-1", 4));

        assertThat(cart.getItems()).containsExactly(Map.entry("P-1", 4));
    }

    @Test
    void createOrUpdate_withZeroQuantity_removesProductFromCart() {
        ShoppingCart cart = cart("c-1", Map.of("p-1", 1, "p-2", 2));
        when(shoppingcartrepo.findByCartid("c-1")).thenReturn(Optional.of(cart));
        stubProduct("p-2", "Mouse");

        ShoppingCartResponse response = controller.createOrUpdateCart(request("c-1", "p-1", 0));

        assertThat(cart.getItems()).containsOnlyKeys("p-2");
        verify(shoppingcartrepo).save(cart);
        assertThat(response.getProducts()).extracting(ProductResponse::getProductid).containsExactly("p-2");
    }

    @Test
    void createOrUpdate_withUnknownCartId_returnsNullAndDoesNotSave() {
        when(shoppingcartrepo.findByCartid("missing")).thenReturn(Optional.empty());

        assertThat(controller.createOrUpdateCart(request("missing", "p-1", 1))).isNull();

        verify(shoppingcartrepo, never()).save(any());
    }

    @Test
    void getCart_skipsProductsThatNoLongerExist() {
        ShoppingCart cart = cart("c-1", Map.of("p-1", 1, "deleted", 2));
        when(shoppingcartrepo.findByCartid("c-1")).thenReturn(Optional.of(cart));
        stubProduct("p-1", "Laptop");
        when(products.findByProductid("deleted")).thenReturn(Optional.empty());

        ShoppingCartResponse response = controller.getCart("c-1");

        assertThat(response.getProducts()).extracting(ProductResponse::getProductid).containsExactly("p-1");
    }

    @Test
    void getCart_withUnknownOrBlankCartId_returnsNull() {
        when(shoppingcartrepo.findByCartid("missing")).thenReturn(Optional.empty());

        assertThat(controller.getCart("missing")).isNull();
        assertThat(controller.getCart(" ")).isNull();
        verify(shoppingcartrepo, times(1)).findByCartid(anyString());
    }

    @Test
    void clearCart_removesAllItemsAndSaves() {
        ShoppingCart cart = cart("c-1", Map.of("p-1", 1, "p-2", 2));
        when(shoppingcartrepo.findByCartid("c-1")).thenReturn(Optional.of(cart));

        ShoppingCartResponse response = controller.clearCart("c-1");

        assertThat(cart.getItems()).isEmpty();
        verify(shoppingcartrepo).save(cart);
        assertThat(response.getCartid()).isEqualTo("c-1");
        assertThat(response.getProducts()).isEmpty();
        verifyNoInteractions(products);
    }

    @Test
    void clearCart_withUnknownCartId_returnsNull() {
        when(shoppingcartrepo.findByCartid("missing")).thenReturn(Optional.empty());

        assertThat(controller.clearCart("missing")).isNull();
        verify(shoppingcartrepo, never()).save(any());
    }

    private void stubProduct(String id, String title) {
        Products product = new Products();
        product.setProductid(id);
        product.setTitle(title);
        product.setPrice("10");
        when(products.findByProductid(id)).thenReturn(Optional.of(product));
    }

    private static ShoppingCart cart(String cartid, Map<String, Integer> items) {
        ShoppingCart cart = new ShoppingCart();
        cart.setCartid(cartid);
        cart.setItems(new HashMap<>(items));
        return cart;
    }

    private static ShoppingCartRequest request(String cartid, String productid, int quantity) {
        ShoppingCartRequest request = new ShoppingCartRequest();
        request.setCartid(cartid);
        request.setProductid(productid);
        request.setQuantity(quantity);
        return request;
    }
}
