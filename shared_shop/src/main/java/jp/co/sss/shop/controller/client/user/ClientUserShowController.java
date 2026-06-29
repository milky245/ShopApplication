package jp.co.sss.shop.controller.client.user;

import jakarta.servlet.http.HttpSession;

import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.DeliveryAddress;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.repository.DeliveryAddressRepository;
import jp.co.sss.shop.repository.LoginHistoryRepository;
import jp.co.sss.shop.util.Constant;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jp.co.sss.shop.repository.UserRepository;

/**
 * 会員詳細表示機能(一般会員用)のコントローラクラスです。
 * @author シュエ　ジーハン
 */
@Controller
public class ClientUserShowController {

	/**
	 * 会員情報リポジトリ
	 */
	@Autowired
	UserRepository userRepository;

	/**
	 * ログイン履歴情報リポジトリ
	 */
	@Autowired
	LoginHistoryRepository loginHistoryRepository;

	/**
	 * お届け先情報リポジトリ
	 */
	@Autowired
	DeliveryAddressRepository deliveryAddressRepository;

	/**
	 * セッション
	 */
	@Autowired
	HttpSession session;

	/**
	 * ログイン会員の詳細画面を表示します。
	 *
	 * @author シュエ　ジーハン
	 * @param model Viewとの値受渡し
	 * @return "client/user/detail" 会員詳細画面
	 * @see jp.co.sss.shop.bean.UserBean
	 * @see jp.co.sss.shop.entity.User
	 */
	@RequestMapping(path = "/client/user/detail", method = { RequestMethod.GET, RequestMethod.POST })
	public String showUser(Model model) {
		UserBean loginUser = (UserBean) session.getAttribute("user");
		if (loginUser == null) {
			return "redirect:/login";
		}

		// ログイン会員IDを条件に最新の会員情報を取得する。
		User user = userRepository.findByIdAndDeleteFlag(loginUser.getId(), Constant.NOT_DELETED);
		if (user == null) {
			return "redirect:/syserror";
		}

		UserBean userBean = new UserBean();
		BeanUtils.copyProperties(user, userBean);
		model.addAttribute("userBean", userBean);

		// 最新3件のログイン履歴を取得し、Modelに追加する
		model.addAttribute("loginHistories", loginHistoryRepository.findTop3ByUserIdOrderByLoginDateTimeDesc(loginUser.getId()));

		// 登録済みのお届け先リストを取得し、Modelに追加する
		model.addAttribute("deliveryAddresses", deliveryAddressRepository.findByUserIdOrderByAddressNo(loginUser.getId()));

		// 詳細画面表示時に会員変更・退会用フォームを初期化する。
		session.removeAttribute("userForm");

		return "client/user/detail";
	}
}
