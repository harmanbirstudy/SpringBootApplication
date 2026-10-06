package com.webapp.shoppingwebsite.controller;

import com.webapp.shoppingwebsite.dao.*;
import com.webapp.shoppingwebsite.payload.OrderIdResponse;
import com.webapp.shoppingwebsite.payload.OrdersRequest;
import com.webapp.shoppingwebsite.repository.OrdersRepository;
import com.webapp.shoppingwebsite.repository.ProductsRepository;
import com.webapp.shoppingwebsite.repository.ShoppingCartRepository;
import com.webapp.shoppingwebsite.repository.UserRepository;
import com.webapp.shoppingwebsite.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderAdminControllerTest {

    private static final UserPrincipal USER =
            new UserPrincipal("u-1", "Jane", "jane@example.com", "hash", true, "", List.of());

    @Mock
    private OrdersRepository ordersrepo;

    @Mock
    private ShoppingCartRepository shoppingcartrepo;

    @Mock
    private ProductsRepository products;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderAdminController controller;

    @Test
    void createOrder_copiesAddressAndCartProductsIntoOrder() {
        ShoppingCart cart = new ShoppingCart();
        cart.setCartid("c-1");
        cart.setItems(new LinkedHashMap<>(Map.of("p-1", 2)));
        when(shoppingcartrepo.findByCartid("c-1")).thenReturn(Optional.of(cart));
        when(products.findByProductid("p-1")).thenReturn(Optional.of(product("p-1", "Laptop", "999")));
        when(ordersrepo.save(any(Orders.class))).thenAnswer(inv -> {
            Orders o = inv.getArgument(0);
            o.setOrderid("o-1");
            return o;
        });

        OrderIdResponse response = controller.createOrder(ordersRequest("c-1"), USER);

        assertThat(response.getOrderid()).isEqualTo("o-1");
        ArgumentCaptor<Orders> captor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersrepo).save(captor.capture());
        Orders saved = captor.getValue();
        assertThat(saved.getUserid()).isEqualTo("u-1");
        assertThat(saved.getName()).isEqualTo("Jane Doe");
        assertThat(saved.getAddline1()).isEqualTo("1 Main St");
        assertThat(saved.getCity()).isEqualTo("Springfield");
        assertThat(saved.getZipcode()).isEqualTo("12345");
        assertThat(saved.getProducts()).singleElement().satisfies(p -> {
            assertThat(p.getTitle()).isEqualTo("Laptop");
            assertThat(p.getPrice()).isEqualTo("999");
            assertThat(p.getQuantity()).isEqualTo(2);
            assertThat(p.getImageurl()).isEqualTo("http://img/p-1.png");
        });
    }

    @Test
    void createOrder_skipsProductsThatNoLongerExist() {
        ShoppingCart cart = new ShoppingCart();
        cart.setItems(new HashMap<>(Map.of("p-1", 1, "deleted", 1)));
        when(shoppingcartrepo.findByCartid("c-1")).thenReturn(Optional.of(cart));
        when(products.findByProductid("p-1")).thenReturn(Optional.of(product("p-1", "Laptop", "999")));
        when(products.findByProductid("deleted")).thenReturn(Optional.empty());
        when(ordersrepo.save(any(Orders.class))).thenAnswer(inv -> inv.getArgument(0));

        controller.createOrder(ordersRequest("c-1"), USER);

        ArgumentCaptor<Orders> captor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersrepo).save(captor.capture());
        assertThat(captor.getValue().getProducts()).extracting(OrderProducts::getTitle).containsExactly("Laptop");
    }

    @Test
    void createOrder_withUnknownCart_savesOrderWithoutProducts() {
        when(shoppingcartrepo.findByCartid("missing")).thenReturn(Optional.empty());
        when(ordersrepo.save(any(Orders.class))).thenAnswer(inv -> inv.getArgument(0));

        controller.createOrder(ordersRequest("missing"), USER);

        ArgumentCaptor<Orders> captor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersrepo).save(captor.capture());
        assertThat(captor.getValue().getProducts()).isEmpty();
        verifyNoInteractions(products);
    }

    @Test
    void createOrder_withBlankCartId_doesNotLookUpCart() {
        when(ordersrepo.save(any(Orders.class))).thenAnswer(inv -> inv.getArgument(0));

        controller.createOrder(ordersRequest(""), USER);

        verifyNoInteractions(shoppingcartrepo, products);
        verify(ordersrepo).save(any(Orders.class));
    }

    @Test
    void getOrderDetails_returnsOrderWhenFound() {
        Orders order = order("o-1", "u-1", new Date());
        when(ordersrepo.findByOrderid("o-1")).thenReturn(Optional.of(order));

        assertThat(controller.getOrderDetails("o-1")).isSameAs(order);
    }

    @Test
    void getOrderDetails_returnsNullWhenMissingOrBlank() {
        when(ordersrepo.findByOrderid("missing")).thenReturn(Optional.empty());

        assertThat(controller.getOrderDetails("missing")).isNull();
        assertThat(controller.getOrderDetails("")).isNull();
        verify(ordersrepo, times(1)).findByOrderid(any());
    }

    @Test
    void getAllUserOrders_returnsCurrentUsersOrdersSortedByDate() {
        Date older = new Date(1_000);
        Date newer = new Date(2_000);
        when(ordersrepo.findByUserid("u-1")).thenReturn(List.of(order("o-new", "u-1", newer), order("o-old", "u-1", older)));

        List<OrderUserIdResponse> result = controller.getOrdersByUser(USER);

        assertThat(result).extracting(OrderUserIdResponse::getOrderid).containsExactly("o-old", "o-new");
        assertThat(result).extracting(OrderUserIdResponse::getOrderdate).containsExactly(older, newer);
    }

    @Test
    void getAllUserOrders_returnsNullWhenUserHasNoOrders() {
        when(ordersrepo.findByUserid("u-1")).thenReturn(List.of());

        assertThat(controller.getOrdersByUser(USER)).isNull();
    }

    @Test
    void getAllOrders_joinsUserNameAndSkipsOrdersWithUnknownUser() {
        when(ordersrepo.findAll()).thenReturn(List.of(
                order("o-2", "u-1", new Date(2_000)),
                order("o-1", "u-2", new Date(1_000)),
                order("o-orphan", "ghost", new Date(3_000))));
        when(userRepository.findByUserid("u-1")).thenReturn(Optional.of(user("u-1", "Jane")));
        when(userRepository.findByUserid("u-2")).thenReturn(Optional.of(user("u-2", "John")));
        when(userRepository.findByUserid("ghost")).thenReturn(Optional.empty());

        List<AllOrdersResponse> result = controller.getAllOrders();

        assertThat(result).extracting(AllOrdersResponse::getOrderid).containsExactly("o-1", "o-2");
        assertThat(result).extracting(AllOrdersResponse::getName).containsExactly("John", "Jane");
    }

    @Test
    void getAllOrders_returnsNullWhenThereAreNoOrders() {
        when(ordersrepo.findAll()).thenReturn(List.of());

        assertThat(controller.getAllOrders()).isNull();
        verifyNoInteractions(userRepository);
    }

    private static OrdersRequest ordersRequest(String cartid) {
        OrdersRequest request = new OrdersRequest();
        request.setCartid(cartid);
        request.setName("Jane Doe");
        request.setAddline1("1 Main St");
        request.setAddline2("Apt 2");
        request.setCity("Springfield");
        request.setState("IL");
        request.setZipcode("12345");
        return request;
    }

    private static Products product(String id, String title, String price) {
        Products product = new Products();
        product.setProductid(id);
        product.setTitle(title);
        product.setPrice(price);
        product.setImageurl("http://img/" + id + ".png");
        return product;
    }

    private static Orders order(String orderid, String userid, Date date) {
        Orders order = new Orders();
        order.setOrderid(orderid);
        order.setUserid(userid);
        order.setOrderdate(date);
        return order;
    }

    private static User user(String userid, String name) {
        User user = new User();
        user.setUserid(userid);
        user.setName(name);
        return user;
    }
}
