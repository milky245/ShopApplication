package jp.co.sss.shop.controller.client.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpSession;

import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.Order;
import jp.co.sss.shop.repository.OrderRepository;
import jp.co.sss.shop.service.BeanTools;
import jp.co.sss.shop.service.PriceCalc;

class ClientOrderShowControllerTest {

	@InjectMocks
	private ClientOrderShowController controller;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private BeanTools beanTools;

	@Mock
	private PriceCalc priceCalc;

	private MockHttpSession session;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		session = new MockHttpSession();
		controller.session = session;
	}

	@Test
	void cancelOrder_OwnOrder_CancelsAndRedirectsToComplete() {
		UserBean user = new UserBean();
		user.setId(10);
		session.setAttribute("user", user);
		Order order = new Order();
		order.setCancelFlag(0);
		when(orderRepository.findByIdAndUserId(100, 10)).thenReturn(order);

		String view = controller.cancelOrder(100);

		assertEquals("redirect:/client/order/cancel/complete", view);
		assertEquals(1, order.getCancelFlag());
		assertNotNull(order.getCancelDate());
		verify(orderRepository).save(order);
	}

	@Test
	void cancelOrder_OtherUserOrder_RedirectsToSystemError() {
		UserBean user = new UserBean();
		user.setId(10);
		session.setAttribute("user", user);
		when(orderRepository.findByIdAndUserId(100, 10)).thenReturn(null);

		String view = controller.cancelOrder(100);

		assertEquals("redirect:/syserror", view);
		verify(orderRepository, never()).save(org.mockito.ArgumentMatchers.any(Order.class));
	}

	@Test
	void cancelOrder_AlreadyCanceled_DoesNotSaveAgain() {
		UserBean user = new UserBean();
		user.setId(10);
		session.setAttribute("user", user);
		Order order = new Order();
		order.setCancelFlag(1);
		when(orderRepository.findByIdAndUserId(100, 10)).thenReturn(order);

		String view = controller.cancelOrder(100);

		assertEquals("redirect:/client/order/cancel/complete", view);
		verify(orderRepository, never()).save(order);
	}
}
