package jp.co.sss.shop.bean;

/**
 * 会員保有クーポンの画面表示用Beanです。
 */
public class UserCouponBean {

	private Integer id;
	private String name;
	private Integer discountRate;
	private Integer minimumOrderAmount;
	private String acquiredAt;
	private String expiresAt;
	private boolean available;
	private String unavailableReason;

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

	public String getAcquiredAt() {
		return acquiredAt;
	}

	public void setAcquiredAt(String acquiredAt) {
		this.acquiredAt = acquiredAt;
	}

	public String getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(String expiresAt) {
		this.expiresAt = expiresAt;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public String getUnavailableReason() {
		return unavailableReason;
	}

	public void setUnavailableReason(String unavailableReason) {
		this.unavailableReason = unavailableReason;
	}
}
