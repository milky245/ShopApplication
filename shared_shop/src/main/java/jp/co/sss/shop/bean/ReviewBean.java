package jp.co.sss.shop.bean;

/**
 * レビュー情報クラス
 */
public class ReviewBean {
	/**
	 * レビューID
	 */
	private Integer id;

	/**
	 * 会員名
	 */
	private String userName;

	/**
	 * 評価
	 */
	private Integer rating;

	/**
	 * レビューコメント
	 */
	private String reviewComment;

	/**
	 * 登録日付
	 */
	private String insertDate;

	/**
	 * 投稿者ID (削除権限制御用)
	 */
	private Integer userId;

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
	 * 会員名の取得
	 * @return 会員名
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * 会員名のセット
	 * @param userName 会員名
	 */
	public void setUserName(String userName) {
		this.userName = userName;
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
	public String getInsertDate() {
		return insertDate;
	}

	/**
	 * 登録日付のセット
	 * @param insertDate 登録日付
	 */
	public void setInsertDate(String insertDate) {
		this.insertDate = insertDate;
	}

	/**
	 * 投稿者IDの取得
	 * @return 投稿者ID
	 */
	public Integer getUserId() {
		return userId;
	}

	/**
	 * 投稿者IDのセット
	 * @param userId 投稿者ID
	 */
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
}
