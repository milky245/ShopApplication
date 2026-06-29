package jp.co.sss.shop.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import jp.co.sss.shop.entity.UserCoupon;

/**
 * user_couponsテーブル用リポジトリです。
 */
@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Integer> {

	@Query("SELECT uc FROM UserCoupon uc JOIN FETCH uc.couponType "
			+ "WHERE uc.user.id = :userId AND uc.expiresAt > :now "
			+ "AND uc.couponType.activeFlag = 1 ORDER BY uc.expiresAt ASC, uc.id ASC")
	List<UserCoupon> findAvailableByUserId(
			@Param("userId") Integer userId, @Param("now") Timestamp now);

	long countByUserIdAndExpiresAtAfter(Integer userId, Timestamp now);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT uc FROM UserCoupon uc WHERE uc.id = :id AND uc.user.id = :userId")
	UserCoupon findByIdAndUserIdForUpdate(
			@Param("id") Integer id, @Param("userId") Integer userId);

	@Query("SELECT uc FROM UserCoupon uc JOIN FETCH uc.couponType "
			+ "WHERE uc.id = :id AND uc.user.id = :userId AND uc.expiresAt > :now")
	UserCoupon findAvailableByIdAndUserId(
			@Param("id") Integer id,
			@Param("userId") Integer userId,
			@Param("now") Timestamp now);

	@Modifying
	@Query("DELETE FROM UserCoupon uc WHERE uc.expiresAt <= :now")
	int deleteExpired(@Param("now") Timestamp now);
}
