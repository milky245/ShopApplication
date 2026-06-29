package jp.co.sss.shop.entity;

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
 * お届け先情報エンティティクラス
 *
 * @author SystemShared
 */
@Entity
@Table(name = "delivery_addresses")
public class DeliveryAddress {

	/**
	 * お届け先ID
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_delivery_addresses_gen")
	@SequenceGenerator(name = "seq_delivery_addresses_gen", sequenceName = "seq_delivery_addresses", allocationSize = 1)
	private Integer id;

	/**
	 * 会員情報
	 */
	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;

	/**
	 * お届け先番号
	 */
	@Column
	private Integer addressNo;

	/**
	 * 氏名
	 */
	@Column
	private String name;

	/**
	 * 郵便番号
	 */
	@Column
	private String postalCode;

	/**
	 * 住所
	 */
	@Column
	private String address;

	/**
	 * 電話番号
	 */
	@Column
	private String phoneNumber;

	/**
	 * お届け先IDの取得
	 * @return お届け先ID
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * お届け先IDのセット
	 * @param id お届け先ID
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * 会員情報の取得
	 * @return 会員情報
	 */
	public User getUser() {
		return user;
	}

	/**
	 * 会員情報のセット
	 * @param user 会員情報
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * お届け先番号の取得
	 * @return お届け先番号
	 */
	public Integer getAddressNo() {
		return addressNo;
	}

	/**
	 * お届け先番号のセット
	 * @param addressNo お届け先番号
	 */
	public void setAddressNo(Integer addressNo) {
		this.addressNo = addressNo;
	}

	/**
	 * 氏名の取得
	 * @return 氏名
	 */
	public String getName() {
		return name;
	}

	/**
	 * 氏名のセット
	 * @param name 氏名
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 郵便番号の取得
	 * @return 郵便番号
	 */
	public String getPostalCode() {
		return postalCode;
	}

	/**
	 * 郵便番号のセット
	 * @param postalCode 郵便番号
	 */
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	/**
	 * 住所の取得
	 * @return 住所
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * 住所のセット
	 * @param address 住所
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * 電話番号の取得
	 * @return 電話番号
	 */
	public String getPhoneNumber() {
		return phoneNumber;
	}

	/**
	 * 電話番号のセット
	 * @param phoneNumber 電話番号
	 */
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
}
