package jp.co.sss.shop.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.entity.ViewHistory;

/**
 * view_historiesテーブル用リポジトリ
 *
 * @author SystemShared
 */
@Repository
public interface ViewHistoryRepository extends JpaRepository<ViewHistory, Integer> {

	/**
	 * 会員と商品に合致する閲覧履歴を取得
	 * @param user 会員情報
	 * @param item 商品情報
	 * @return 閲覧履歴
	 */
	public ViewHistory findByUserAndItem(User user, Item item);

	/**
	 * 会員ごとの閲覧履歴を新しい順に取得
	 * @param user 会員情報
	 * @param currentItem 除外する現在表示中の商品
	 * @param pageable ページング情報
	 * @return 閲覧履歴のリスト
	 */
	@Query("SELECT v.item FROM ViewHistory v WHERE v.user = :user AND v.item <> :currentItem AND v.item.deleteFlag = 0 ORDER BY v.viewDate DESC")
	public List<Item> findItemsByUser(@Param("user") User user, @Param("currentItem") Item currentItem, Pageable pageable);
}
