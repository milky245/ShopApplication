package jp.co.sss.shop.repository;

import java.sql.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jp.co.sss.shop.entity.CouponGachaHistory;

/**
 * coupon_gacha_historiesテーブル用リポジトリです。
 */
@Repository
public interface CouponGachaHistoryRepository extends JpaRepository<CouponGachaHistory, Integer> {

	boolean existsByUserIdAndBusinessDate(Integer userId, Date businessDate);
}
