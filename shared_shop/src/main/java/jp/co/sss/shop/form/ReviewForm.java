package jp.co.sss.shop.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * レビュー情報のフォームクラス
 */
public class ReviewForm {

	/**
	 * 注文商品ID
	 */
	@NotNull
	private Integer orderItemId;

	/**
	 * 商品ID
	 */
	@NotNull
	private Integer itemId;

	/**
	 * 評価
	 */
	@NotNull(message = "{rating.notnull}")
	@Min(value = 1)
	@Max(value = 5)
	private Integer rating;

	/**
	 * レビューコメント
	 */
	@Size(max = 2000)
	private String reviewComment;

	/**
	 * 注文商品IDの取得
	 * @return 注文商品ID
	 */
	public Integer getOrderItemId() {
		return orderItemId;
	}

	/**
	 * 注文商品IDのセット
	 * @param orderItemId 注文商品ID
	 */
	public void setOrderItemId(Integer orderItemId) {
		this.orderItemId = orderItemId;
	}

	/**
	 * 商品IDの取得
	 * @return 商品ID
	 */
	public Integer getItemId() {
		return itemId;
	}

	/**
	 * 商品IDのセット
	 * @param itemId 商品ID
	 */
	public void setItemId(Integer itemId) {
		this.itemId = itemId;
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
}
