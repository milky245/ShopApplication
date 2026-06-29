package jp.co.sss.shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jp.co.sss.shop.entity.CouponType;

/**
 * coupon_typesテーブル用リポジトリです。
 */
@Repository
public interface CouponTypeRepository extends JpaRepository<CouponType, Integer> {

	List<CouponType> findByActiveFlagOrderByDiscountRateAsc(Integer activeFlag);

	CouponType findByDiscountRateAndActiveFlag(Integer discountRate, Integer activeFlag);
}
