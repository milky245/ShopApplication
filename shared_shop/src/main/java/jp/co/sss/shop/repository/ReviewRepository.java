package jp.co.sss.shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.sss.shop.entity.Review;

/**
 * reviewsテーブル用リポジトリ
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

	/**
	 * 商品IDを条件に、投稿日時の降順でレビューを検索
	 * @param itemId 商品ID
	 * @return レビューエンティティのリスト
	 */
	List<Review> findByItemIdOrderByInsertDateDesc(Integer itemId);

	/**
	 * 商品IDを条件に、評価の降順（高評価順）でレビューを検索
	 * @param itemId 商品ID
	 * @return レビューエンティティのリスト
	 */
	List<Review> findByItemIdOrderByRatingDescInsertDateDesc(Integer itemId);

	/**
	 * 商品IDを条件に、評価の昇順（低評価順）でレビューを検索
	 * @param itemId 商品ID
	 * @return レビューエンティティのリスト
	 */
	List<Review> findByItemIdOrderByRatingAscInsertDateDesc(Integer itemId);

	/**
	 * 注文商品IDを条件にレビューを検索
	 * @param orderItemId 注文商品ID
	 * @return レビューエンティティ
	 */
	Review findByOrderItemId(Integer orderItemId);

	/**
	 * 商品IDを条件に、平均評価を取得
	 * @param itemId 商品ID
	 * @return 平均評価
	 */
	@Query("SELECT AVG(r.rating) FROM Review r WHERE r.item.id = :itemId")
	Double getAvgRatingByItemId(@Param("itemId") Integer itemId);

	/**
	 * 商品IDを条件に、レビュー件数を取得
	 * @param itemId 商品ID
	 * @return レビュー件数
	 */
	@Query("SELECT COUNT(r) FROM Review r WHERE r.item.id = :itemId")
	Long getReviewCountByItemId(@Param("itemId") Integer itemId);

	/**
	 * 商品IDのリストを条件に、平均評価とレビュー件数を取得
	 * @param itemIds 商品IDのリスト
	 * @return アイテムID、平均評価、レビュー件数の配列リスト
	 */
	@Query("SELECT r.item.id, AVG(r.rating), COUNT(r) FROM Review r WHERE r.item.id IN :itemIds GROUP BY r.item.id")
	List<Object[]> findReviewStatsByItemIds(@Param("itemIds") List<Integer> itemIds);
}
