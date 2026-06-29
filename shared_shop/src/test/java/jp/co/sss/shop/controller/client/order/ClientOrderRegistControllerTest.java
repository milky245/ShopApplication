package jp.co.sss.shop.controller.client.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jp.co.sss.shop.bean.BasketBean;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.CouponType;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.entity.Order;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.entity.UserCoupon;
import jp.co.sss.shop.form.OrderForm;
import jp.co.sss.shop.repository.ItemRepository;
import jp.co.sss.shop.repository.OrderItemRepository;
import jp.co.sss.shop.repository.OrderRepository;
import jp.co.sss.shop.repository.UserRepository;
import jp.co.sss.shop.repository.UserCouponRepository;
import jp.co.sss.shop.service.BeanTools;
import jp.co.sss.shop.service.PriceCalc;
import jp.co.sss.shop.util.Constant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

class ClientOrderRegistControllerTest {

    @InjectMocks
    private ClientOrderRegistController controller;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCouponRepository userCouponRepository;

    @Mock
    private BeanTools beanTools;

    @Mock
    private PriceCalc priceCalc;

    @Mock
    private org.springframework.context.MessageSource messageSource;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = new MockHttpSession();
        controller.session = session;
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("error message");
    }

    @Test
    void orderComplete_Success_RedirectsToComplete() {
        OrderForm orderForm = new OrderForm();
        orderForm.setId(1);
        orderForm.setPostalCode("1234567");
        orderForm.setAddress("Test Address");
        orderForm.setName("Test Name");
        orderForm.setPhoneNumber("09012345678");
        orderForm.setPayMethod(1);
        orderForm.setDeliveryDate("2026-06-25");
        session.setAttribute("orderForm", orderForm);

		UserBean loginUser = new UserBean();
		loginUser.setId(1);
		session.setAttribute("user", loginUser);

        List<BasketBean> basketBeans = new ArrayList<>();
        basketBeans.add(new BasketBean(1, "Test Item", 100, 1));
        session.setAttribute("basketBeans", basketBeans);

        Item item = new Item();
        item.setId(1);
        item.setName("Test Item");
        item.setStock(10);
        item.setPrice(100);
        when(itemRepository.findByIdAndDeleteFlag(1, Constant.NOT_DELETED)).thenReturn(item);

        Order order = new Order();
        order.setId(100);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        User user = new User();
        user.setId(1);
        user.setPoint(500);
        when(userRepository.findByIdAndDeleteFlag(1, Constant.NOT_DELETED)).thenReturn(user);

        String view = controller.orderComplete();

        assertEquals("redirect:/client/order/complete", view);
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).save(any());
        verify(itemRepository).save(any(Item.class));
    }

	@Test
	void orderCheck_UsableCoupon_SavesDiscountSnapshot() {
		OrderForm orderForm = new OrderForm();
		orderForm.setId(1);
		session.setAttribute("orderForm", orderForm);
		UserBean loginUser = new UserBean();
		loginUser.setId(1);
		session.setAttribute("user", loginUser);

		List<BasketBean> basketBeans = List.of(new BasketBean(1, "Test Item", 10, 2));
		session.setAttribute("basketBeans", basketBeans);
		Item item = new Item();
		item.setId(1);
		item.setPrice(500);
		when(itemRepository.findByIdAndDeleteFlag(1, Constant.NOT_DELETED)).thenReturn(item);

		CouponType type = new CouponType();
		type.setName("10％割引クーポン");
		type.setDiscountRate(10);
		type.setMinimumOrderAmount(1000);
		type.setActiveFlag(1);
		UserCoupon coupon = new UserCoupon();
		coupon.setId(9);
		coupon.setCouponType(type);
		coupon.setExpiresAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().plusDays(1)));
		when(userCouponRepository.findAvailableByIdAndUserId(eq(9), eq(1), any())).thenReturn(coupon);

		String view = controller.orderCheck(1, 9, 0);

		assertEquals("redirect:/client/order/check", view);
		assertEquals(9, orderForm.getCouponId());
		assertEquals(100, orderForm.getCouponDiscountAmount());
		assertEquals("10％割引クーポン", orderForm.getCouponName());
	}

	@Test
	void orderCheck_MinimumAmountNotMet_RejectsCoupon() {
		OrderForm orderForm = new OrderForm();
		orderForm.setId(1);
		session.setAttribute("orderForm", orderForm);
		UserBean loginUser = new UserBean();
		loginUser.setId(1);
		session.setAttribute("user", loginUser);
		session.setAttribute("basketBeans", List.of(new BasketBean(1, "Test Item", 10, 1)));
		Item item = new Item();
		item.setId(1);
		item.setPrice(999);
		when(itemRepository.findByIdAndDeleteFlag(1, Constant.NOT_DELETED)).thenReturn(item);

		CouponType type = new CouponType();
		type.setDiscountRate(10);
		type.setMinimumOrderAmount(1000);
		type.setActiveFlag(1);
		UserCoupon coupon = new UserCoupon();
		coupon.setId(9);
		coupon.setCouponType(type);
		coupon.setExpiresAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().plusDays(1)));
		when(userCouponRepository.findAvailableByIdAndUserId(eq(9), eq(1), any())).thenReturn(coupon);

		String view = controller.orderCheck(1, 9, 0);

		assertEquals("redirect:/client/order/payment/input", view);
		assertNull(orderForm.getCouponId());
		assertEquals(0, orderForm.getCouponDiscountAmount());
	}

    @Test
    void addressInputCheck_DeliveryDate_RangeError_Before() {
        OrderForm lastForm = new OrderForm();
        session.setAttribute("orderForm", lastForm);

        OrderForm form = new OrderForm();
        LocalDate today = LocalDate.now();
        form.setDeliveryDate(today.plusDays(2).toString()); // 2 days later (invalid)

        BindingResult result = new BeanPropertyBindingResult(form, "orderForm");

        String view = controller.addressInputCheck(form, result);

        assertEquals("redirect:/client/order/address/input", view);
        assertTrue(result.hasFieldErrors("deliveryDate"));
        assertEquals("orderForm.deliveryDate.invalid", result.getFieldError("deliveryDate").getCode());
    }

    @Test
    void addressInputCheck_DeliveryDate_RangeError_After() {
        OrderForm lastForm = new OrderForm();
        session.setAttribute("orderForm", lastForm);

        OrderForm form = new OrderForm();
        LocalDate today = LocalDate.now();
        form.setDeliveryDate(today.plusDays(15).toString()); // 15 days later (invalid)

        BindingResult result = new BeanPropertyBindingResult(form, "orderForm");

        String view = controller.addressInputCheck(form, result);

        assertEquals("redirect:/client/order/address/input", view);
        assertTrue(result.hasFieldErrors("deliveryDate"));
        assertEquals("orderForm.deliveryDate.invalid", result.getFieldError("deliveryDate").getCode());
    }

    @Test
    void addressInputCheck_DeliveryDate_Valid() {
        OrderForm lastForm = new OrderForm();
        session.setAttribute("orderForm", lastForm);

        OrderForm form = new OrderForm();
        LocalDate today = LocalDate.now();
        form.setDeliveryDate(today.plusDays(3).toString()); // 3 days later (valid)

        BindingResult result = new BeanPropertyBindingResult(form, "orderForm");

        String view = controller.addressInputCheck(form, result);

        assertEquals("redirect:/client/order/payment/input", view);
        assertNull(result.getFieldError("deliveryDate"));
    }

    @Test
    void orderCheck_NegativePoint_ReturnsError() {
        OrderForm orderForm = new OrderForm();
        orderForm.setId(1);
        session.setAttribute("orderForm", orderForm);
        UserBean loginUser = new UserBean();
        loginUser.setId(1);
        session.setAttribute("user", loginUser);

        String view = controller.orderCheck(1, null, -100);

        assertEquals("redirect:/client/order/payment/input", view);
        assertEquals("error message", session.getAttribute("pointError"));
    }

    @Test
    void orderCheck_PointBelow100_ReturnsError() {
        OrderForm orderForm = new OrderForm();
        orderForm.setId(1);
        session.setAttribute("orderForm", orderForm);
        UserBean loginUser = new UserBean();
        loginUser.setId(1);
        session.setAttribute("user", loginUser);

        User user = new User();
        user.setPoint(500);
        when(userRepository.findByIdAndDeleteFlag(1, Constant.NOT_DELETED)).thenReturn(user);

        String view = controller.orderCheck(1, null, 50);

        assertEquals("redirect:/client/order/payment/input", view);
        assertEquals("error message", session.getAttribute("pointError"));
    }

    @Test
    void orderCheck_PointExceedsHold_ReturnsError() {
        OrderForm orderForm = new OrderForm();
        orderForm.setId(1);
        session.setAttribute("orderForm", orderForm);
        UserBean loginUser = new UserBean();
        loginUser.setId(1);
        session.setAttribute("user", loginUser);

        User user = new User();
        user.setPoint(500);
        when(userRepository.findByIdAndDeleteFlag(1, Constant.NOT_DELETED)).thenReturn(user);

        String view = controller.orderCheck(1, null, 600);

        assertEquals("redirect:/client/order/payment/input", view);
        assertEquals("error message", session.getAttribute("pointError"));
    }

    @Test
    void addressInputCheck_DeliveryDate_Empty_Valid_From_BusinessCheck() {
        OrderForm lastForm = new OrderForm();
        session.setAttribute("orderForm", lastForm);

        OrderForm form = new OrderForm();
        form.setDeliveryDate(""); // Not specified

        BindingResult result = new BeanPropertyBindingResult(form, "orderForm");

        String view = controller.addressInputCheck(form, result);

        assertEquals("redirect:/client/order/payment/input", view);
        assertNull(result.getFieldError("deliveryDate"));
    }

    @Test
    void orderFormValidation_AllowsEmptyDeliveryDate() {
        OrderForm form = new OrderForm();
        form.setPostalCode("1234567");
        form.setAddress("Test Address");
        form.setName("Test Name");
        form.setPhoneNumber("09012345678");
        form.setDeliveryDate("");

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<OrderForm>> violations = validator.validate(form);

        assertFalse(violations.stream()
                .anyMatch(violation -> "deliveryDate".equals(violation.getPropertyPath().toString())));
    }

    @Test
    void addressInputCheck_DeliveryDate_InvalidFormat() {
        OrderForm lastForm = new OrderForm();
        session.setAttribute("orderForm", lastForm);

        OrderForm form = new OrderForm();
        form.setDeliveryDate("2026/06/25"); // Invalid format

        BindingResult result = new BeanPropertyBindingResult(form, "orderForm");

        String view = controller.addressInputCheck(form, result);

        assertEquals("redirect:/client/order/address/input", view);
        assertTrue(result.hasFieldErrors("deliveryDate"));
        assertEquals("orderForm.deliveryDate.invalid_format", result.getFieldError("deliveryDate").getCode());
    }
}
