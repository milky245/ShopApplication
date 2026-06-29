package jp.co.sss.shop.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * 会員が保有するクーポンのエンティティクラスです。
 */
@Entity
@Table(name = "user_coupons")
public class UserCoupon {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_user_coupons_gen")
	@SequenceGenerator(name = "seq_user_coupons_gen", sequenceName = "seq_user_coupons", allocationSize = 1)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;

	@ManyToOne
	@JoinColumn(name = "coupon_type_id", referencedColumnName = "id")
	private CouponType couponType;

	@Column(insertable = false)
	private Timestamp acquiredAt;

	@Column
	private Timestamp expiresAt;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public CouponType getCouponType() {
		return couponType;
	}

	public void setCouponType(CouponType couponType) {
		this.couponType = couponType;
	}

	public Timestamp getAcquiredAt() {
		return acquiredAt;
	}

	public void setAcquiredAt(Timestamp acquiredAt) {
		this.acquiredAt = acquiredAt;
	}

	public Timestamp getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Timestamp expiresAt) {
		this.expiresAt = expiresAt;
	}
}
