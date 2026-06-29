package jp.co.sss.shop.config;

import java.sql.Timestamp;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jp.co.sss.shop.repository.UserCouponRepository;

/**
 * 期限切れの会員保有クーポンを定期削除します。
 */
@Component
public class CouponExpirationCleanup {

	private final UserCouponRepository userCouponRepository;

	public CouponExpirationCleanup(UserCouponRepository userCouponRepository) {
		this.userCouponRepository = userCouponRepository;
	}

	@Transactional
	@Scheduled(cron = "0 0 * * * *", zone = "Asia/Tokyo")
	public void deleteExpiredCoupons() {
		userCouponRepository.deleteExpired(new Timestamp(System.currentTimeMillis()));
	}
}
