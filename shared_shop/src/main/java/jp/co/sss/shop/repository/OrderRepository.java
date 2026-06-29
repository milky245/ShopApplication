package jp.co.sss.shop.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.sss.shop.entity.Order;

/**
 * ordersテーブル用リポジトリ
 *
 * @author System Shared
 */
@Repository

public interface OrderRepository extends JpaRepository<Order, Integer> {

	/**
	 * 注文日付降順で注文情報すべてを検索(管理者機能で利用)
	 * @param pageable ページング情報
	 * @return 注文エンティティのページオブジェクト
	 */
	@Query("SELECT o FROM Order o ORDER BY o.insertDate DESC,o.id DESC")
	Page<Order> findAllOrderByInsertdateDescIdDesc(Pageable pageable);

	/**
	 * 会員IDを条件に注文日付降順で注文情報を検索します。
	 * 注文一覧で利用
	 * @param userId 会員ID
	 * @return 注文エンティティのページオブジェクト
	 */
	@Query("SELECT o FROM Order o WHERE o.user.id =:userId ORDER BY o.insertDate DESC,o.id DESC")
	List<Order> findByUserIdOrderByInsertDateDescIdDesc(
			@Param(value = "userId") Integer userId);

	/**
	 * 注文IDと会員IDを条件に注文情報を検索します。
	 * 注文詳細で利用
	 * @param id 注文ID
	 * @param userId 会員ID
	 * @return 注文エンティティ
	 */
	@Query("SELECT o FROM Order o WHERE o.id =:id AND o.user.id =:userId")
	Order findByIdAndUserId(
			@Param(value = "id") Integer id,
			@Param(value = "userId") Integer userId);

}