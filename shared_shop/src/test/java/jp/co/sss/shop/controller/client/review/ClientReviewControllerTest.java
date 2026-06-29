package jp.co.sss.shop.controller.client.review;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.entity.Order;
import jp.co.sss.shop.entity.OrderItem;
import jp.co.sss.shop.entity.Review;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.repository.OrderItemRepository;
import jp.co.sss.shop.repository.ReviewRepository;

public class ClientReviewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private HttpSession session;

    @InjectMocks
    private ClientReviewController clientReviewController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(clientReviewController).build();
    }

    @Test
    public void testRegistInput_Success() throws Exception {
        UserBean userBean = new UserBean();
        userBean.setId(1);

        User user = new User();
        user.setId(1);

        Order order = new Order();
        order.setId(10);
        order.setUser(user);

        Item item = new Item();
        item.setId(100);
        item.setName("Test Item");

        OrderItem orderItem = new OrderItem();
        orderItem.setId(1000);
        orderItem.setOrder(order);
        orderItem.setItem(item);

        when(session.getAttribute("user")).thenReturn(userBean);
        when(orderItemRepository.findById(1000)).thenReturn(Optional.of(orderItem));
        when(reviewRepository.findByOrderItemId(1000)).thenReturn(null);

        mockMvc.perform(get("/client/review/regist/input/1000")
                .sessionAttr("user", userBean))
                .andExpect(status().isOk())
                .andExpect(view().name("client/review/regist_input"))
                .andExpect(model().attributeExists("reviewForm"))
                .andExpect(model().attributeExists("orderItem"));
    }

    @Test
    public void testRegistInput_OtherUserOrder() throws Exception {
        UserBean userBean = new UserBean();
        userBean.setId(1);

        User otherUser = new User();
        otherUser.setId(2);

        Order order = new Order();
        order.setUser(otherUser);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);

        when(session.getAttribute("user")).thenReturn(userBean);
        when(orderItemRepository.findById(1000)).thenReturn(Optional.of(orderItem));

        mockMvc.perform(get("/client/review/regist/input/1000")
                .sessionAttr("user", userBean))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/syserror"));
    }
}
