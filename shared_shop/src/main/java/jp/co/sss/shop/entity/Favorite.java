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
 * お気に入り情報エンティティクラス
 */
@Entity
@Table(name = "favorite")
public class Favorite {

	/**
	 * お気に入りID
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_favorite_gen")
	@SequenceGenerator(name = "seq_favorite_gen", sequenceName = "seq_favorite", allocationSize = 1)
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
	 * 登録日時
	 */
	@Column(insertable = false, updatable = false)
	private Timestamp insertDate;

	/**
	 * お気に入りIDの取得
	 * @return お気に入りID
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * お気に入りIDのセット
	 * @param id お気に入りID
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
	 * 商品情報の取得
	 * @return 商品情報
	 */
	public Item getItem() {
		return item;
	}

	/**
	 * 商品情報のセット
	 * @param item 商品情報
	 */
	public void setItem(Item item) {
		this.item = item;
	}

	/**
	 * 登録日時の取得
	 * @return 登録日時
	 */
	public Timestamp getInsertDate() {
		return insertDate;
	}

	/**
	 * 登録日時のセット
	 * @param insertDate 登録日時
	 */
	public void setInsertDate(Timestamp insertDate) {
		this.insertDate = insertDate;
	}
}
