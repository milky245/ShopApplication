package jp.co.sss.shop.entity;

import java.sql.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * 注文情報のエンティティクラス
 *
 * @author SystemShared
 */
@Entity
@Table(name = "orders")
public class Order {
	/**
	 * 注文ID
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_orders_gen")
	@SequenceGenerator(name = "seq_orders_gen", sequenceName = "seq_orders", allocationSize = 1)
	private Integer id;

	/**
	 * 送付先郵便番号
	 */
	@Column
	private String postalCode;

	/**
	 * 送付先住所
	 */
	@Column
	private String address;

	/**
	 * 送付先宛名
	 */
	@Column
	private String name;

	/**
	 * 送付先電話番号
	 */
	@Column
	private String phoneNumber;

	/**
	 * 支払方法
	 */
	@Column
	private Integer payMethod;

	/**
	 * 注文日付
	 */
	@Column(insertable = false)
	private Date insertDate;

	/**
	 * 配送希望日
	 */
	@Column
	private Date deliveryDate;

	/**
	 * 注文取消フラグ
	 */
	@Column
	private Integer cancelFlag = 0;

	/**
	 * 注文取消日
	 */
	@Column
	private Date cancelDate;

	/** 利用したクーポン種別 */
	@ManyToOne
	@JoinColumn(name = "coupon_type_id", referencedColumnName = "id")
	private CouponType couponType;

	/** 注文時点のクーポン名称 */
	@Column
	private String couponName;

	/** 注文時点のクーポン割引率 */
	@Column
	private Integer couponDiscountRate;

	/** 注文時点のクーポン割引額 */
	@Column
	private Integer couponDiscountAmount;

	/** 利用ポイント */
	@Column
	private Integer usePoint = 0;

	/** 付与ポイント */
	@Column
	private Integer earnedPoint = 0;

	/**
	 * 会員情報
	 */
	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;

	/**
	 * 注文商品リスト
	 */
	@OneToMany(mappedBy = "order")
	private List<OrderItem> orderItemsList;

	/**
	 * 注文IDの取得
	 * @return 注文ID
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * 注文IDのセット
	 * @param id 注文ID
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * お届け先郵便番号の取得
	 * @return お届け先郵便番号
	 */
	public String getPostalCode() {
		return postalCode;
	}

	/**
	 * お届け先郵便番号のセット
	 * @param postalCode お届け先郵便番号
	 */
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	/**
	 * お届け先住所の取得
	 * @return お届け先住所
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * お届け先住所のセット
	 * @param address お届け先住所
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * お届け先氏名の取得
	 * @return お届け先氏名
	 */
	public String getName() {
		return name;
	}

	/**
	 * お届け先氏名のセット
	 * @param name お届け先氏名
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * お届け先電話番号の取得
	 * @return お届け先電話番号
	 */
	public String getPhoneNumber() {
		return phoneNumber;
	}

	/**
	 * お届け先電話番号のセット
	 * @param phoneNumber お届け先電話番号
	 */
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	/**
	 * 支払方法の取得
	 * @return 支払方法
	 */
	public Integer getPayMethod() {
		return payMethod;
	}

	/**
	 * 支払い方法のセット
	 * @param payMethod 支払方法
	 */
	public void setPayMethod(Integer payMethod) {
		this.payMethod = payMethod;
	}

	/**
	 * 登録日付の取得
	 * @return 登録日付
	 */
	public Date getInsertDate() {
		return insertDate;
	}

	/**
	 * 登録日付のセット
	 * @param insertDate 登録日付
	 */
	public void setInsertDate(Date insertDate) {
		this.insertDate = insertDate;
	}

	/**
	 * 配送希望日の取得
	 * @return 配送希望日
	 */
	public Date getDeliveryDate() {
		return deliveryDate;
	}

	/**
	 * 配送希望日のセット
	 * @param deliveryDate 配送希望日
	 */
	public void setDeliveryDate(Date deliveryDate) {
		this.deliveryDate = deliveryDate;
	}

	public Integer getCancelFlag() {
		return cancelFlag;
	}

	public void setCancelFlag(Integer cancelFlag) {
		this.cancelFlag = cancelFlag;
	}

	public Date getCancelDate() {
		return cancelDate;
	}

	public void setCancelDate(Date cancelDate) {
		this.cancelDate = cancelDate;
	}

	public CouponType getCouponType() {
		return couponType;
	}

	public void setCouponType(CouponType couponType) {
		this.couponType = couponType;
	}

	public String getCouponName() {
		return couponName;
	}

	public void setCouponName(String couponName) {
		this.couponName = couponName;
	}

	public Integer getCouponDiscountRate() {
		return couponDiscountRate;
	}

	public void setCouponDiscountRate(Integer couponDiscountRate) {
		this.couponDiscountRate = couponDiscountRate;
	}

	public Integer getCouponDiscountAmount() {
		return couponDiscountAmount;
	}

	public void setCouponDiscountAmount(Integer couponDiscountAmount) {
		this.couponDiscountAmount = couponDiscountAmount;
	}

	public Integer getUsePoint() {
		return usePoint;
	}

	public void setUsePoint(Integer usePoint) {
		this.usePoint = usePoint;
	}

	public Integer getEarnedPoint() {
		return earnedPoint;
	}

	public void setEarnedPoint(Integer earnedPoint) {
		this.earnedPoint = earnedPoint;
	}

	/**
	 * 会員エンティティの取得
	 * @return 会員エンティティ
	 */
	public User getUser() {
		return user;
	}

	/**
	 * 会員エンティティのセット
	 * @param user 会員エンティティ
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * 注文IDに紐づく注文商品エンティティのリストを取得
	 * @return 注文商品エンティティのリスト
	 */
	public List<OrderItem> getOrderItemsList() {
		return orderItemsList;
	}

	/**
	 * 注文商品エンティティのリストをセット
	 * @param orderItemsList 注文商品エンティティのリスト
	 */
	public void setOrderItemsList(List<OrderItem> orderItemsList) {
		this.orderItemsList = orderItemsList;
	}

}
