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
 * 閲覧履歴情報のエンティティクラス
 *
 * @author SystemShared
 */
@Entity
@Table(name = "view_histories")
public class ViewHistory {

	/**
	 * 閲覧履歴ID
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_view_histories_gen")
	@SequenceGenerator(name = "seq_view_histories_gen", sequenceName = "seq_view_histories", allocationSize = 1)
	private Integer id;

	/**
	 * 会員情報
	 */
	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;

	/**
	 * 商品情報
	 */
	@ManyToOne
	@JoinColumn(name = "item_id", referencedColumnName = "id")
	private Item item;

	/**
	 * 閲覧日時
	 */
	@Column(insertable = false)
	private Timestamp viewDate;

	/**
	 * 閲覧履歴IDの取得
	 * @return 閲覧履歴ID
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * 閲覧履歴IDのセット
	 * @param id 閲覧履歴ID
	 */
	public void setId(Integer id) {
		this.id = id;
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
	 * 商品エンティティの取得
	 * @return 商品エンティティ
	 */
	public Item getItem() {
		return item;
	}

	/**
	 * 商品エンティティのセット
	 * @param item 商品エンティティ
	 */
	public void setItem(Item item) {
		this.item = item;
	}

	/**
	 * 閲覧日時の取得
	 * @return 閲覧日時
	 */
	public Timestamp getViewDate() {
		return viewDate;
	}

	/**
	 * 閲覧日時のセット
	 * @param viewDate 閲覧日時
	 */
	public void setViewDate(Timestamp viewDate) {
		this.viewDate = viewDate;
	}

}
