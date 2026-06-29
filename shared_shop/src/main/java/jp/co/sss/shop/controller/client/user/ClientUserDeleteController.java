package jp.co.sss.shop.controller.client.user;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.form.UserForm;
import jp.co.sss.shop.repository.UserRepository;
import jp.co.sss.shop.util.Constant;

/**
 * 会員退会機能(一般会員用)のコントローラクラスです。
 *
 * @author 金宮 永茉
 */
@Controller
public class ClientUserDeleteController {

	/** 会員情報リポジトリ */
	@Autowired
	UserRepository userRepository;

	/** セッション */
	@Autowired
	HttpSession session;

	/**
	 * 退会確認画面表示用の会員フォームを初期化するメソッド
	 *
	 * @author 金宮 永茉
	 * @return "redirect:/client/user/delete/check" 退会確認画面表示処理へリダイレクト
	 */
	@RequestMapping(path = "/client/user/delete/check", method = RequestMethod.POST)
	public String deleteCheckInit() {

		// セッションのログイン会員情報を元に退会確認用フォームを生成し、セッションへ保存する。
		UserBean loginUser = (UserBean) session.getAttribute("user");
		if (loginUser == null) {
			return "redirect:/login";
		}

		User user = userRepository.findByIdAndDeleteFlag(loginUser.getId(), Constant.NOT_DELETED);
		if (user == null) {
			return "redirect:/syserror";
		}

		UserForm userForm = new UserForm();
		BeanUtils.copyProperties(user, userForm);
		session.setAttribute("userForm", userForm);
		return "redirect:/client/user/delete/check";
	}

	/**
	 * 退会確認画面を表示するメソッド
	 *
	 * @author 金宮 永茉
	 * @param model Viewとの値受渡し
	 * @return "client/user/delete_check" 退会確認画面
	 */
	@RequestMapping(path = "/client/user/delete/check", method = RequestMethod.GET)
	public String deleteCheck(Model model) {

		// セッションから退会確認用フォームを取得し、画面表示用に設定する。
		UserForm userForm = (UserForm) session.getAttribute("userForm");
		if (userForm == null) {
			return "redirect:/client/user/detail";
		}

		model.addAttribute("userForm", userForm);
		return "client/user/delete_check";
	}

	/**
	 * ログイン会員の退会処理を行うメソッド
	 *
	 * @author 金宮 永茉
	 * @return "redirect:/client/user/delete/complete" 退会完了画面表示処理へリダイレクト
	 */
	@RequestMapping(path = "/client/user/delete/complete", method = RequestMethod.POST)
	public String deleteComplete() {

		// 会員情報の削除フラグを更新し、データベースへ反映する。
		UserForm userForm = (UserForm) session.getAttribute("userForm");
		if (userForm == null) {
			return "redirect:/client/user/detail";
		}

		User user = userRepository.findByIdAndDeleteFlag(userForm.getId(), Constant.NOT_DELETED);
		if (user == null) {
			return "redirect:/syserror";
		}

		user.setDeleteFlag(Constant.DELETED);
		userRepository.save(user);

		// セッションを破棄し、ログイン状態を終了する。
		session.invalidate();
		return "redirect:/client/user/delete/complete";
	}

	/**
	 * 退会完了画面を表示するメソッド
	 *
	 * @author 金宮 永茉
	 * @return "client/user/delete_complete" 退会完了画面
	 */
	@RequestMapping(path = "/client/user/delete/complete", method = RequestMethod.GET)
	public String deleteCompleteFinish() {
		return "client/user/delete_complete";
	}
}