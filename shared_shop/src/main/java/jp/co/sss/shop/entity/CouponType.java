package jp.co.sss.shop.entity;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * クーポン種別マスターのエンティティクラスです。
 */
@Entity
@Table(name = "coupon_types")
public class CouponType {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_coupon_types_gen")
	@SequenceGenerator(name = "seq_coupon_types_gen", sequenceName = "seq_coupon_types", allocationSize = 1)
	private Integer id;

	@Column
	private String name;

	@Column
	private Integer discountRate;

	@Column
	private Integer minimumOrderAmount;

	@Column
	private Integer validityDays;

	@Column(insertable = false)
	private Integer activeFlag;

	@Column(insertable = false)
	private Date insertDate;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getDiscountRate() {
		return discountRate;
	}

	public void setDiscountRate(Integer discountRate) {
		this.discountRate = discountRate;
	}

	public Integer getMinimumOrderAmount() {
		return minimumOrderAmount;
	}

	public void setMinimumOrderAmount(Integer minimumOrderAmount) {
		this.minimumOrderAmount = minimumOrderAmount;
	}

	public Integer getValidityDays() {
		return validityDays;
	}

	public void setValidityDays(Integer validityDays) {
		this.validityDays = validityDays;
	}

	public Integer getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(Integer activeFlag) {
		this.activeFlag = activeFlag;
	}

	public Date getInsertDate() {
		return insertDate;
	}

	public void setInsertDate(Date insertDate) {
		this.insertDate = insertDate;
	}
}
