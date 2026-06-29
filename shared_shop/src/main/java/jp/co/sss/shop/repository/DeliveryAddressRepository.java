package jp.co.sss.shop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jp.co.sss.shop.entity.DeliveryAddress;

/**
 * お届け先情報リポジトリインタフェース
 *
 * @author SystemShared
 */
@Repository
public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Integer> {

	/**
	 * 指定ユーザーのお届け先一覧を取得する（お届け先番号順）
	 * @param userId ユーザーID
	 * @return お届け先リスト
	 */
	List<DeliveryAddress> findByUserIdOrderByAddressNo(Integer userId);

	/**
	 * 指定ユーザーのお届け先件数を取得する
	 * @param userId ユーザーID
	 * @return 件数
	 */
	long countByUserId(Integer userId);

	/**
	 * 指定ユーザーの特定お届け先を取得する
	 * @param id お届け先ID
	 * @param userId ユーザーID
	 * @return お届け先情報
	 */
	Optional<DeliveryAddress> findByIdAndUserId(Integer id, Integer userId);
}
