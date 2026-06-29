package jp.co.sss.shop.controller.client.coupon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpSession;

import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.CouponType;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.entity.UserCoupon;
import jp.co.sss.shop.repository.CouponGachaHistoryRepository;
import jp.co.sss.shop.repository.CouponTypeRepository;
import jp.co.sss.shop.repository.UserCouponRepository;
import jp.co.sss.shop.repository.UserRepository;
import jp.co.sss.shop.util.Constant;

class ClientCouponControllerTest {

	@InjectMocks
	private ClientCouponController controller;

	@Mock
	private CouponTypeRepository couponTypeRepository;

	@Mock
	private UserCouponRepository userCouponRepository;

	@Mock
	private CouponGachaHistoryRepository couponGachaHistoryRepository;

	@Mock
	private UserRepository userRepository;

	private MockHttpSession session;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		session = new MockHttpSession();
		controller.session = session;
		UserBean loginUser = new UserBean();
		loginUser.setId(1);
		session.setAttribute("user", loginUser);
		User user = new User();
		user.setId(1);
		when(userRepository.findByIdAndDeleteFlagForUpdate(1, Constant.NOT_DELETED)).thenReturn(user);
	}

	@Test
	void draw_MaximumCouponCount_DoesNotConsumeDailyChance() {
		when(userCouponRepository.countByUserIdAndExpiresAtAfter(any(), any()))
				.thenReturn((long) ClientCouponController.MAX_COUPON_COUNT);

		String view = controller.draw();

		assertEquals("redirect:/client/coupon/gacha", view);
		assertEquals("保有上限の20枚に達しているため、新しいクーポンを獲得できません。",
				session.getAttribute("couponMessage"));
		verify(couponGachaHistoryRepository, never()).save(any());
		verify(userCouponRepository, never()).save(any());
	}

	@Test
	void draw_WinningCoupon_SavesCouponWithExpiry() {
		when(userCouponRepository.countByUserIdAndExpiresAtAfter(any(), any())).thenReturn(0L);
		when(couponGachaHistoryRepository.existsByUserIdAndBusinessDate(any(), any())).thenReturn(false);
		CouponType type = new CouponType();
		type.setName("15％割引クーポン");
		type.setDiscountRate(15);
		type.setMinimumOrderAmount(2000);
		type.setValidityDays(30);
		ClientCouponController spyController = org.mockito.Mockito.spy(controller);
		org.mockito.Mockito.doReturn(type).when(spyController).drawCouponType();

		String view = spyController.draw();

		assertEquals("redirect:/client/coupon/gacha", view);
		verify(couponGachaHistoryRepository).save(any());
		verify(userCouponRepository).save(org.mockito.ArgumentMatchers.argThat(coupon -> {
			Timestamp lowerBound = Timestamp.valueOf(LocalDateTime.now().plusDays(29));
			return coupon instanceof UserCoupon
					&& ((UserCoupon) coupon).getCouponType() == type
					&& ((UserCoupon) coupon).getExpiresAt().after(lowerBound);
		}));
	}
}
