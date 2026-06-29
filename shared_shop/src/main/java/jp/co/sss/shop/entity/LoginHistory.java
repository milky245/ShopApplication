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
 * ログイン履歴情報エンティティクラス
 *
 * @author SystemShared
 */
@Entity
@Table(name = "login_histories")
public class LoginHistory {

	/**
	 * ログイン履歴ID
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_login_histories_gen")
	@SequenceGenerator(name = "seq_login_histories_gen", sequenceName = "seq_login_histories", allocationSize = 1)
	private Integer id;

	/**
	 * ログイン日時
	 */
	@Column
	private Timestamp loginDateTime;

	/**
	 * IPアドレス
	 */
	@Column
	private String ipAddress;

	/**
	 * 会員情報
	 */
	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;

	/**
	 * ログイン履歴IDの取得
	 * @return ログイン履歴ID
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * ログイン履歴IDのセット
	 * @param id ログイン履歴ID
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * ログイン日時の取得
	 * @return ログイン日時
	 */
	public Timestamp getLoginDateTime() {
		return loginDateTime;
	}

	/**
	 * ログイン日時のセット
	 * @param loginDateTime ログイン日時
	 */
	public void setLoginDateTime(Timestamp loginDateTime) {
		this.loginDateTime = loginDateTime;
	}

	/**
	 * IPアドレスの取得
	 * @return IPアドレス
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * IPアドレスのセット
	 * @param ipAddress IPアドレス
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
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
}
