package jp.co.sss.shop.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jp.co.sss.shop.entity.Favorite;

/**
 * favoriteテーブル用リポジトリ
 */
@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {

	/**
	 * 会員IDに紐づくお気に入り情報を登録日時降順で取得
	 * N+1問題を回避するため商品情報をフェッチ
	 * @param userId 会員ID
	 * @return お気に入り情報のリスト
	 */
	@Query("SELECT f FROM Favorite f JOIN FETCH f.item WHERE f.user.id = :userId ORDER BY f.insertDate DESC")
	List<Favorite> findByUserIdOrderByInsertDateDesc(@Param("userId") Integer userId);

	/**
	 * 会員IDと商品IDに紐づくお気に入り情報の存在確認
	 * @param userId 会員ID
	 * @param itemId 商品ID
	 * @return 存在する場合はtrue、そうでない場合はfalse
	 */
	boolean existsByUserIdAndItemId(Integer userId, Integer itemId);

	/**
	 * 会員IDと商品IDに紐づくお気に入り情報を削除
	 * @param userId 会員ID
	 * @param itemId 商品ID
	 */
	void deleteByUserIdAndItemId(Integer userId, Integer itemId);
}
