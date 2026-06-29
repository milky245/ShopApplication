package jp.co.sss.shop.form;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * お届け先情報入力フォーム
 *
 * @author SystemShared
 */
public class DeliveryAddressForm implements Serializable {

	/**
	 * お届け先ID
	 */
	private Integer id;

	/**
	 * 氏名
	 */
	@NotBlank
	@Size(min = 1, max = 30, message = "{text.maxsize.message}")
	private String name;

	/**
	 * 郵便番号
	 */
	@NotBlank
	@Size(min = 7, max = 7, message = "{text.fixsize.message}")
	@Pattern(regexp = "^[0-9]+$", message = "{userRegist.numberpattern.message}")
	private String postalCode;

	/**
	 * 住所
	 */
	@NotBlank
	@Size(min = 1, max = 150, message = "{text.maxsize.message}")
	private String address;

	/**
	 * 電話番号
	 */
	@NotBlank
	@Size(min = 10, max = 11)
	@Pattern(regexp = "^[0-9]+$", message = "{userRegist.numberpattern.message}")
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
