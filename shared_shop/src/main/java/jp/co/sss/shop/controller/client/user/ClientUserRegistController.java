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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.form.UserForm;
import jp.co.sss.shop.repository.UserRepository;

/**
 * 会員登録機能(一般会員用)のコントローラクラスです。
 *
 * @author 小暮 太陽
 * @see jp.co.sss.shop.form.UserForm
 */
@Controller
public class ClientUserRegistController {

	private static final String[] USER_FORM_FIELD_ORDER = {
			"email", "password", "name", "postalCode", "address", "phoneNumber"
	};

	/**
	 * 会員情報リポジトリ
	 */
	@Autowired
	UserRepository userRepository;

	/**
	 * セッション
	 */
	@Autowired
	HttpSession session;

	/**
	 * 新規会員登録フォームを初期化します。
	 *
	 * @author 小暮 太陽
	 * @see jp.co.sss.shop.form.UserForm
	 * @return "redirect:/client/user/regist/input" 登録入力画面表示処理へリダイレクト
	 */
	@RequestMapping(path = "/client/user/regist/input/init", method = RequestMethod.GET)
	public String userRegistInputLink() {
		// 新規に入力フォーム情報を生成し、セッションスコープに保存する
		session.setAttribute("userForm", new UserForm());

		// 登録入力画面表示処理にリダイレクトする
		return "redirect:/client/user/regist/input";
	}

	/**
	 * 登録入力画面へ戻ります。
	 *
	 * @author 小暮 太陽
	 * @see jp.co.sss.shop.form.UserForm
	 * @return "redirect:/client/user/regist/input" 登録入力画面表示処理へリダイレクト
	 */
	@RequestMapping(path = "/client/user/regist/input", method = RequestMethod.POST)
	public String userRegistInputButton() {
		if (session.getAttribute("userForm") == null) {
			return "redirect:/login";
		}
		return "redirect:/client/user/regist/input";
	}

	/**
	 * 登録入力画面を表示します。
	 *
	 * @author 小暮 太陽
	 * @see jp.co.sss.shop.form.UserForm
	 * @param model Viewとの値受渡し
	 * @return "client/user/regist_input" 登録入力画面
	 */
	@RequestMapping(path = "/client/user/regist/input", method = RequestMethod.GET)
	public String userRegistInput(Model model) {
		// セッションスコープから入力フォーム情報を取得
		UserForm userForm = (UserForm) session.getAttribute("userForm");

		// 入力フォーム情報をリクエストスコープに設定
		// (リダイレクトされたエラー用のフォーム情報がない場合のみ設定する)
		if (!model.containsAttribute("userForm")) {
			model.addAttribute("userForm", userForm);
		}

		// 登録入力画面を表示する
		return "client/user/regist_input";
	}

	/**
	 * 登録入力値をチェックし、登録確認画面へ遷移します。
	 *
	 * @author 小暮 太陽
	 * @see jp.co.sss.shop.form.UserForm
	 * @param form 会員入力フォーム
	 * @param result 入力チェック結果
	 * @param redirectAttributes リダイレクト先へエラー情報を引き継ぐためのオブジェクト
	 * @return 入力エラーあり: "redirect:/client/user/regist/input"、なし: "redirect:/client/user/regist/check"
	 */
	@RequestMapping(path = "/client/user/regist/check", method = RequestMethod.POST)
	public String userRegistCheckButton(@Valid @ModelAttribute UserForm form, BindingResult result, RedirectAttributes redirectAttributes) {
		// 画面から入力された会員情報を取得した入力フォーム情報に設定し、セッションスコープに保存
		session.setAttribute("userForm", form);

		// 入力値のチェック結果判定
		if (result.hasErrors()) {
			// リダイレクト仕様を満たすため、Springの標準機能でエラー結果を一時保存して引き継ぐ
			redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userForm",
					createSortedBindingResult(form, result));
			redirectAttributes.addFlashAttribute("userForm", form);

			// エラーあり：登録入力画面表示処理にリダイレクトする
			return "redirect:/client/user/regist/input";
		}

		// エラーなし：登録確認画面表示処理にリダイレクトする
		return "redirect:/client/user/regist/check";
	}

	/**
	 * 登録確認画面を表示します。
	 *
	 * @author 小暮 太陽
	 * @see jp.co.sss.shop.form.UserForm
	 * @param model Viewとの値受渡し
	 * @return "client/user/regist_check" 登録確認画面
	 */
	@RequestMapping(path = "/client/user/regist/check", method = RequestMethod.GET)
	public String userRegistCheck(Model model) {
		// セッションスコープから入力フォーム情報を取得
		UserForm userForm = (UserForm) session.getAttribute("userForm");
		if (userForm == null) {
			return "redirect:/login";
		}

		// 入力フォーム情報をリクエストスコープに設定
		model.addAttribute("userForm", userForm);

		// 登録確認画面を表示する
		return "client/user/regist_check";
	}

	/**
	 * 会員情報を登録します。
	 *
	 * @author 小暮 太陽
	 * @see jp.co.sss.shop.entity.User
	 * @see jp.co.sss.shop.bean.UserBean
	 * @return "redirect:/client/user/regist/complete" 登録完了画面表示処理へリダイレクト
	 */
	@RequestMapping(path = "/client/user/regist/complete", method = RequestMethod.POST)
	public String userRegistCompleteButton() {
		// セッションスコープから入力フォーム情報を取得
		UserForm userForm = (UserForm) session.getAttribute("userForm");
		if (userForm == null) {
			return "redirect:/login";
		}

		// 入力フォーム情報を元にDB登録用エンティティオブジェクトを生成
		User user = new User();

		// BeanToolsに会員用メソッドがないため、Spring標準の機能でコピーを行う
		BeanUtils.copyProperties(userForm, user);

		// 一般会員権限(2)を設定
		user.setAuthority(2);

		// DB登録を実施する
		userRepository.save(user);

		// セッションスコープの入力フォーム情報削除
		session.removeAttribute("userForm");

		// 未ログインでの会員登録の場合、セッションスコープに会員情報をセットしログイン状態にする
		UserBean userBean = new UserBean();
		BeanUtils.copyProperties(user, userBean);
		session.setAttribute("user", userBean);

		// 登録完了画面表示処理にリダイレクトする
		return "redirect:/client/user/regist/complete";
	}

	/**
	 * 登録完了画面を表示します。
	 *
	 * @author 小暮 太陽
	 * @see jp.co.sss.shop.bean.UserBean
	 * @return "client/user/regist_complete" 登録完了画面
	 */
	@RequestMapping(path = "/client/user/regist/complete", method = RequestMethod.GET)
	public String userRegistComplete() {
		// 登録完了画面を表示する（フォワード）
		return "client/user/regist_complete";
	}

	private BindingResult createSortedBindingResult(UserForm form, BindingResult result) {
		BindingResult sortedResult = new BeanPropertyBindingResult(form, result.getObjectName());
		List<ObjectError> errors = new ArrayList<ObjectError>(result.getAllErrors());
		errors.sort(Comparator.comparingInt(this::userFormFieldOrder));
		for (ObjectError error : errors) {
			sortedResult.addError(error);
		}
		return sortedResult;
	}

	private int userFormFieldOrder(ObjectError error) {
		if (! (error instanceof FieldError fieldError)) {
			return USER_FORM_FIELD_ORDER.length;
		}
		for (int i = 0; i < USER_FORM_FIELD_ORDER.length; i++) {
			if (USER_FORM_FIELD_ORDER[i].equals(fieldError.getField())) {
				return i;
			}
		}
		return USER_FORM_FIELD_ORDER.length;
	}
}
