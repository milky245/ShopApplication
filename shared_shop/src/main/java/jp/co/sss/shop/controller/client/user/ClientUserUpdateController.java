package jp.co.sss.shop.controller.client.user;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.form.UserForm;
import jp.co.sss.shop.repository.UserRepository;

/**
 * 会員変更機能(一般会員用)のコントローラクラスです。
 *
 * @author 金宮 永茉
 */
@Controller
public class ClientUserUpdateController {

	/**
	 * UserFormの入力項目名を定義した配列
	 * 空欄補完処理で各項目を順番に参照する際に使用する
	 */
	private static final String[] USER_FORM_FIELD_ORDER = {
			"email", "password", "name", "postalCode", "address", "phoneNumber"
	};

	/** 会員情報リポジトリ */
	@Autowired
	UserRepository userRepository;

	/** セッション */
	@Autowired
	HttpSession session;

	/**
	 * 変更入力画面表示用の会員フォームを初期化するメソッド
	 * 
	 * @author 金宮 永茉
	 * @return "redirect:/client/user/update/input" 変更入力画面表示処理へリダイレクト
	 */
	@RequestMapping(path = "/client/user/update/input", method = RequestMethod.POST)
	public String updateInputInit() {

		// セッションに入力フォーム情報がない場合、
		// ログイン会員情報をもとにUserFormを生成しセッションへ保存する。
		if (session.getAttribute("userForm") == null) {
			UserBean userBean = (UserBean) session.getAttribute("user");
			User user = userRepository.getReferenceById(userBean.getId());
			UserForm userForm = new UserForm();
			BeanUtils.copyProperties(user, userForm);

			// パスワードは初期表示しない
			userForm.setPassword("");
			session.setAttribute("userForm", userForm);
		}
		return "redirect:/client/user/update/input";
	}

	/**
	 * 変更入力画面を表示するメソッド
	 *
	 * @author 金宮 永茉
	 * @param model Viewとの値受渡し
	 * @return "client/user/update_input" 変更入力画面
	 */
	@RequestMapping(path = "/client/user/update/input", method = RequestMethod.GET)
	public String updateInput(Model model) {

		// セッションの入力フォーム情報を取得し、画面へ渡す。
		UserForm userForm = (UserForm) session.getAttribute("userForm");
		model.addAttribute("userForm", userForm);

		// セッションに入力エラー情報がある場合は画面へ渡し、表示後に削除する。
		if (session.getAttribute("result") != null) {
			model.addAttribute("org.springframework.validation.BindingResult.userForm", session.getAttribute("result"));
			session.removeAttribute("result");
		}
		return "client/user/update_input";
	}

	/**
	 * 変更入力値をチェックし、変更確認画面へ遷移するメソッド
	 *
	 * @author 金宮 永茉
	 * @param form 会員入力フォーム
	 * @param result 入力チェック結果
	 * @return 入力エラーあり: "redirect:/client/user/update/input"、なし: "redirect:/client/user/update/check"
	 */
	@RequestMapping(path = "/client/user/update/check", method = RequestMethod.POST)
	public String updateInputCheck(@Valid @ModelAttribute UserForm form, BindingResult result) {

		// 入力フォーム情報をセッションへ保存する。
		session.setAttribute("userForm", form);

		// エラー判定
		// エラーがある場合、エラー情報をセッションに保存し入力画面にリダイレクト。
		if (result.hasErrors()) {
			session.setAttribute("result", createSortedBindingResult(form, result));
			return "redirect:/client/user/update/input";
		}

		// 入力エラー情報を削除し、確認画面にリダイレクト。
		session.removeAttribute("result");
		return "redirect:/client/user/update/check";
	}

	/**
	 * 変更確認画面を表示するメソッド
	 *
	 * @author 金宮 永茉
	 * @param model Viewとの値受渡し
	 * @return "client/user/update_check" 変更確認画面
	 */
	@RequestMapping(path = "/client/user/update/check", method = RequestMethod.GET)
	public String updateCheck(Model model) {

		// セッションから入力フォーム情報を取得し、画面表示用に設定する。
		UserForm userForm = (UserForm) session.getAttribute("userForm");
		model.addAttribute("userForm", userForm);
		return "client/user/update_check";
	}

	/**
	 * 会員情報を更新するメソッド
	 *
	 * @author 金宮 永茉
	 * @return "redirect:/client/user/update/complete" 変更完了画面表示処理へリダイレクト
	 */
	@RequestMapping(path = "/client/user/update/complete", method = RequestMethod.POST)
	public String updateComplete() {

		// セッションの入力フォーム情報をもとに会員情報を更新し、データベースへ反映する。
		UserForm userForm = (UserForm) session.getAttribute("userForm");
		User user = userRepository.getReferenceById(userForm.getId());
		BeanUtils.copyProperties(userForm, user);
		userRepository.save(user);

		// 入力フォーム情報を削除し、セッションのログイン会員情報を更新する。
		session.removeAttribute("userForm");
		UserBean userBean = new UserBean();
		BeanUtils.copyProperties(user, userBean);
		session.setAttribute("user", userBean);
		return "redirect:/client/user/update/complete";
	}

	/**
	 * 変更完了画面を表示するメソッド
	 *
	 * @author 金宮 永茉
	 * @return "client/user/update_complete" 変更完了画面
	 */
	@RequestMapping(path = "/client/user/update/complete", method = RequestMethod.GET)
	public String updateCompleteFinish() {
		return "client/user/update_complete";
	}

	/**
	 * 入力エラーメッセージをUserFormの項目順に並び替えるメソッド
	 * 
	 * @author シュエ ジーハン
	 * @param form 入力フォーム
	 * @param result 元の入力チェック結果
	 * @return 項目順に並び替えた入力チェック結果
	 */
	private BindingResult createSortedBindingResult(UserForm form, BindingResult result) {

		// 入力エラーをフォーム項目の表示順に並び替えたBindingResultを生成する。
		BindingResult sortedResult = new BeanPropertyBindingResult(form, result.getObjectName());
		List<ObjectError> errors = new ArrayList<ObjectError>(result.getAllErrors());
		errors.sort(Comparator.comparingInt(this::userFormFieldOrder));

		// 並び替えた入力エラーを新しいBindingResultへ追加する。
		for (ObjectError error : errors) {
			sortedResult.addError(error);
		}
		return sortedResult;
	}

	/**
	 * 入力エラーに対応するUserFormの項目順を取得するメソッド
	 * 
	 * @author シュエ ジーハン
	 * @param error
	 * @return USER_FORM_FIELD_ORDERにおける項目の位置
	 */
	private int userFormFieldOrder(ObjectError error) {

		// 入力エラーに対応するフォーム項目の表示順を取得する。
		if (!(error instanceof FieldError fieldError)) {
			return 0;
		}

		// 項目名と一致する位置を検索し、表示順を返す。
		for (int i = 0; i < USER_FORM_FIELD_ORDER.length; i++) {
			if (USER_FORM_FIELD_ORDER[i].equals(fieldError.getField())) {
				return i;
			}
		}

		// 定義されていない項目は最後に配置する。
		return USER_FORM_FIELD_ORDER.length;
	}
}
