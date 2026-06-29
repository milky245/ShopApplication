package jp.co.sss.shop.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.sss.shop.entity.Item;

/**
 * itemsテーブル用リポジトリ
 *
 * @author System Shared
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {

	/**
	 * 商品情報を登録日付順に取得 管理者機能で利用
	 * @param deleteFlag 削除フラグ
	 * @param pageable ページング情報
	 * @return 商品エンティティのページオブジェクト
	 */
	@Query("SELECT i FROM Item i INNER JOIN i.category c WHERE i.deleteFlag =:deleteFlag ORDER BY i.insertDate DESC,i.id DESC")
	Page<Item> findByDeleteFlagOrderByInsertDateDescPage(
			@Param(value = "deleteFlag") int deleteFlag, Pageable pageable);

	/**
	 * 商品情報を登録日付降順で取得します。
	 * 切通 隆晟担当: 商品一覧（新着）/トップ画面（売れ筋順改修）で利用します。
	 * @param deleteFlag 削除フラグ
	 * @return 商品エンティティのリスト
	 */
	@Query("SELECT i FROM Item i INNER JOIN i.category c WHERE i.deleteFlag =:deleteFlag ORDER BY i.insertDate DESC,i.id DESC")
	List<Item> findByDeleteFlagOrderByInsertDateDesc(
			@Param(value = "deleteFlag") int deleteFlag);

	/**
	 * 商品情報をカテゴリ別、登録日付降順で取得します。
	 * コグレ担当: 商品検索（カテゴリ）で利用します。
	 * @param categoryId カテゴリID
	 * @param deleteFlag 削除フラグ
	 * @return 商品エンティティのリスト
	 */
	@Query("SELECT i FROM Item i INNER JOIN i.category c WHERE c.id =:categoryId AND i.deleteFlag =:deleteFlag ORDER BY i.insertDate DESC,i.id DESC")
	List<Item> findByCategoryIdAndDeleteFlagOrderByInsertDateDesc(
			@Param(value = "categoryId") Integer categoryId,
			@Param(value = "deleteFlag") int deleteFlag);

	/**
	 * 売れ筋順の商品情報を取得します。
	 * シュエ ジーハン担当: 商品一覧（売れ筋）で利用します。
	 * @param deleteFlag 削除フラグ
	 * @return 商品エンティティのリスト
	 */
	@Query("SELECT i FROM Item i INNER JOIN i.category c INNER JOIN i.orderItemList oi WHERE i.deleteFlag =:deleteFlag GROUP BY i ORDER BY SUM(oi.quantity) DESC,i.id ASC")
	List<Item> findHotSellItems(
			@Param(value = "deleteFlag") int deleteFlag);

	/**
	 * カテゴリ別に売れ筋順の商品情報を取得します。
	 * シュエ ジーハン担当/コグレ担当: 売れ筋順とカテゴリ検索を組み合わせる場合に利用します。
	 * @param categoryId カテゴリID
	 * @param deleteFlag 削除フラグ
	 * @return 商品エンティティのリスト
	 */
	@Query("SELECT i FROM Item i INNER JOIN i.category c INNER JOIN i.orderItemList oi WHERE c.id =:categoryId AND i.deleteFlag =:deleteFlag GROUP BY i ORDER BY SUM(oi.quantity) DESC,i.id ASC")
	List<Item> findHotSellItemsByCategoryId(
			@Param(value = "categoryId") Integer categoryId,
			@Param(value = "deleteFlag") int deleteFlag);

	/**
	 * 同一カテゴリの商品を累計注文数量の多い順に取得します。
	 * 注文実績がない商品も候補に含めます。
	 *
	 * @param categoryId カテゴリID
	 * @param currentItemId 現在表示中の商品ID
	 * @param deleteFlag 削除フラグ
	 * @param pageable ページング情報
	 * @return 関連商品エンティティのリスト
	 */
	@Query("""
			SELECT i
			FROM Item i
			INNER JOIN i.category c
			LEFT JOIN i.orderItemList oi
			WHERE c.id = :categoryId
			  AND i.id <> :currentItemId
			  AND i.deleteFlag = :deleteFlag
			  AND c.deleteFlag = :deleteFlag
			  AND i.stock > 0
			GROUP BY i
			ORDER BY COALESCE(SUM(oi.quantity), 0) DESC,
			         i.insertDate DESC,
			         i.id DESC
			""")
	List<Item> findRelatedItems(
			@Param(value = "categoryId") Integer categoryId,
			@Param(value = "currentItemId") Integer currentItemId,
			@Param(value = "deleteFlag") int deleteFlag,
			Pageable pageable);

	/**
	 * 商品IDと削除フラグを条件に検索（管理者,商品詳細機能で利用）
	 * @param id 商品ID
	 * @param deleteFlag 削除フラグ
	 * @return 商品エンティティ
	 */
	public Item findByIdAndDeleteFlag(Integer id, int deleteFlag);

	/**
	 * 商品名と削除フラグを条件に検索 (ItemValidatorで利用)
	 * @param name 商品名
	 * @param notDeleted 削除フラグ
	 * @return 商品エンティティ
	 */
	public Item findByNameAndDeleteFlag(String name, int notDeleted);
}
