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
 * レビュー情報のエンティティクラス
 */
@Entity
@Table(name = "reviews")
public class Review {
	/**
	 * レビューID
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_reviews_gen")
	@SequenceGenerator(name = "seq_reviews_gen", sequenceName = "seq_reviews", allocationSize = 1)
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
	 * 注文商品情報
	 */
	@ManyToOne
	@JoinColumn(name = "order_item_id", referencedColumnName = "id")
	private OrderItem orderItem;

	/**
	 * 評価
	 */
	@Column
	private Integer rating;

	/**
	 * レビューコメント
	 */
	@Column(name = "review_comment")
	private String reviewComment;

	/**
	 * 登録日付
	 */
	@Column(name = "insert_date", insertable = false, updatable = false)
	private Timestamp insertDate;

	/**
	 * レビューIDの取得
	 * @return レビューID
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * レビューIDのセット
	 * @param id レビューID
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
	 * 注文商品情報の取得
	 * @return 注文商品情報
	 */
	public OrderItem getOrderItem() {
		return orderItem;
	}

	/**
	 * 注文商品情報のセット
	 * @param orderItem 注文商品情報
	 */
	public void setOrderItem(OrderItem orderItem) {
		this.orderItem = orderItem;
	}

	/**
	 * 評価の取得
	 * @return 評価
	 */
	public Integer getRating() {
		return rating;
	}

	/**
	 * 評価のセット
	 * @param rating 評価
	 */
	public void setRating(Integer rating) {
		this.rating = rating;
	}

	/**
	 * レビューコメントの取得
	 * @return レビューコメント
	 */
	public String getReviewComment() {
		return reviewComment;
	}

	/**
	 * レビューコメントのセット
	 * @param reviewComment レビューコメント
	 */
	public void setReviewComment(String reviewComment) {
		this.reviewComment = reviewComment;
	}

	/**
	 * 登録日付の取得
	 * @return 登録日付
	 */
	public Timestamp getInsertDate() {
		return insertDate;
	}

	/**
	 * 登録日付のセット
	 * @param insertDate 登録日付
	 */
	public void setInsertDate(Timestamp insertDate) {
		this.insertDate = insertDate;
	}
}
