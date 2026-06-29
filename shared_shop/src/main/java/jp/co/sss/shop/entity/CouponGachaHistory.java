package jp.co.sss.shop.entity;

import java.sql.Date;
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
 * クーポンガチャ実行履歴のエンティティクラスです。
 */
@Entity
@Table(name = "coupon_gacha_histories")
public class CouponGachaHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_coupon_gacha_histories_gen")
	@SequenceGenerator(name = "seq_coupon_gacha_histories_gen", sequenceName = "seq_coupon_gacha_histories", allocationSize = 1)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;

	@Column
	private Date businessDate;

	@ManyToOne
	@JoinColumn(name = "result_coupon_type_id", referencedColumnName = "id")
	private CouponType resultCouponType;

	@Column(insertable = false)
	private Timestamp drawnAt;

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

	public Date getBusinessDate() {
		return businessDate;
	}

	public void setBusinessDate(Date businessDate) {
		this.businessDate = businessDate;
	}

	public CouponType getResultCouponType() {
		return resultCouponType;
	}

	public void setResultCouponType(CouponType resultCouponType) {
		this.resultCouponType = resultCouponType;
	}

	public Timestamp getDrawnAt() {
		return drawnAt;
	}

	public void setDrawnAt(Timestamp drawnAt) {
		this.drawnAt = drawnAt;
	}
}
