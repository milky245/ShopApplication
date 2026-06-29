package jp.co.sss.shop.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jp.co.sss.shop.entity.LoginHistory;

/**
 * ログイン履歴情報リポジトリインタフェース
 *
 * @author SystemShared
 */
@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Integer> {

	/**
	 * 指定ユーザーの最新3件のログイン履歴を日時降順で取得
	 * @param userId 会員ID
	 * @return ログイン履歴のリスト
	 */
	List<LoginHistory> findTop3ByUserIdOrderByLoginDateTimeDesc(Integer userId);

	/**
	 * 指定ユーザーの過去1年分のログイン履歴を日時降順で取得
	 * @param userId 会員ID
	 * @param oneYearAgo 1年前の日時
	 * @return ログイン履歴のリスト
	 */
	List<LoginHistory> findByUserIdAndLoginDateTimeGreaterThanEqualOrderByLoginDateTimeDesc(Integer userId, Timestamp oneYearAgo);
}
