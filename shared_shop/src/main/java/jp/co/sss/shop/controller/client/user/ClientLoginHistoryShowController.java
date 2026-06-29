package jp.co.sss.shop.controller.client.user;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.repository.LoginHistoryRepository;

/**
 * ログイン履歴表示機能(一般会員用)のコントローラクラスです。
 *
 * @author SystemShared
 */
@Controller
public class ClientLoginHistoryShowController {

	/**
	 * ログイン履歴情報リポジトリ
	 */
	@Autowired
	LoginHistoryRepository loginHistoryRepository;

	/**
	 * セッション
	 */
	@Autowired
	HttpSession session;

	/**
	 * ログイン会員の過去1年分のログイン履歴を表示します。
	 *
	 * @param model Viewとの値受渡し
	 * @return "client/user/login_history_list" ログイン履歴一覧画面
	 */
	@RequestMapping(path = "/client/user/login_history", method = { RequestMethod.GET, RequestMethod.POST })
	public String showLoginHistory(Model model) {
		UserBean loginUser = (UserBean) session.getAttribute("user");
		if (loginUser == null) {
			return "redirect:/login";
		}

		// 1年前の日時を計算
		Timestamp oneYearAgo = Timestamp.valueOf(LocalDateTime.now().minusYears(1));

		// 過去1年分のログイン履歴を取得し、Modelに追加する
		model.addAttribute("loginHistories", loginHistoryRepository.findByUserIdAndLoginDateTimeGreaterThanEqualOrderByLoginDateTimeDesc(loginUser.getId(), oneYearAgo));

		return "client/user/login_history_list";
	}
}
