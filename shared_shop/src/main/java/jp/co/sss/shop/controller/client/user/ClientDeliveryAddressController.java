package jp.co.sss.shop.controller.client.user;

import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.DeliveryAddress;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.form.DeliveryAddressForm;
import jp.co.sss.shop.repository.DeliveryAddressRepository;

/**
 * お届け先管理機能(一般会員用)のコントローラクラスです。
 *
 * @author SystemShared
 */
@Controller
public class ClientDeliveryAddressController {

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
	 * 登録入力画面を表示します。
	 *
	 * @param model Viewとの値受渡し
	 * @return "client/user/address/regist_input" 登録入力画面
	 */
	@RequestMapping(path = "/client/delivery_address/regist/input", method = RequestMethod.POST)
	public String registInput(Model model) {
		UserBean loginUser = (UserBean) session.getAttribute("user");
		if (loginUser == null) {
			return "redirect:/login";
		}

		long count = deliveryAddressRepository.countByUserId(loginUser.getId());
		if (count >= 3) {
			return "redirect:/client/user/detail";
		}

		DeliveryAddressForm form = (DeliveryAddressForm) session.getAttribute("deliveryAddressForm");
		if (form == null) {
			form = new DeliveryAddressForm();
		}
		model.addAttribute("deliveryAddressForm", form);

		return "client/user/address/regist_input";
	}

	/**
	 * 登録内容を確認します。
	 *
	 * @param form お届け先情報入力フォーム
	 * @param result 入力チェック結果
	 * @return 入力エラーあり: "client/user/address/regist_input"
	 *         なし: "client/user/address/regist_check"
	 */
	@RequestMapping(path = "/client/delivery_address/regist/check", method = RequestMethod.POST)
	public String registCheck(@Valid @ModelAttribute DeliveryAddressForm form, BindingResult result) {
		if (result.hasErrors()) {
			return "client/user/address/regist_input";
		}

		session.setAttribute("deliveryAddressForm", form);
		return "client/user/address/regist_check";
	}

	/**
	 * 登録を完了します。
	 *
	 * @return "client/user/address/regist_complete" 登録完了画面
	 */
	@RequestMapping(path = "/client/delivery_address/regist/complete", method = RequestMethod.POST)
	public String registComplete() {
		UserBean loginUser = (UserBean) session.getAttribute("user");
		DeliveryAddressForm form = (DeliveryAddressForm) session.getAttribute("deliveryAddressForm");

		if (loginUser == null || form == null) {
			return "redirect:/syserror";
		}

		List<DeliveryAddress> addresses = deliveryAddressRepository.findByUserIdOrderByAddressNo(loginUser.getId());
		if (addresses.size() >= 3) {
			return "redirect:/client/user/detail";
		}

		// 空いているaddress_noを特定する(1~3)
		int addressNo = 1;
		boolean[] used = new boolean[4];
		for (DeliveryAddress addr : addresses) {
			used[addr.getAddressNo()] = true;
		}
		for (int i = 1; i <= 3; i++) {
			if (!used[i]) {
				addressNo = i;
				break;
			}
		}

		DeliveryAddress deliveryAddress = new DeliveryAddress();
		BeanUtils.copyProperties(form, deliveryAddress);
		deliveryAddress.setAddressNo(addressNo);

		User user = new User();
		user.setId(loginUser.getId());
		deliveryAddress.setUser(user);

		deliveryAddressRepository.save(deliveryAddress);

		session.removeAttribute("deliveryAddressForm");
		return "client/user/address/regist_complete";
	}

	/**
	 * 編集入力画面を表示します。
	 *
	 * @param id お届け先ID
	 * @param model Viewとの値受渡し
	 * @return "client/user/address/update_input" 編集入力画面
	 */
	@RequestMapping(path = "/client/delivery_address/update/input/{id}", method = RequestMethod.POST)
	public String updateInput(@PathVariable Integer id, Model model) {
		UserBean loginUser = (UserBean) session.getAttribute("user");
		if (loginUser == null) {
			return "redirect:/login";
		}

		Optional<DeliveryAddress> opt = deliveryAddressRepository.findByIdAndUserId(id, loginUser.getId());
		if (opt.isEmpty()) {
			return "redirect:/client/user/detail";
		}

		DeliveryAddressForm form = (DeliveryAddressForm) session.getAttribute("deliveryAddressForm");
		if (form == null || !id.equals(form.getId())) {
			form = new DeliveryAddressForm();
			BeanUtils.copyProperties(opt.get(), form);
		}
		model.addAttribute("deliveryAddressForm", form);

		return "client/user/address/update_input";
	}

	/**
	 * 編集内容を確認します。
	 *
	 * @param form お届け先情報入力フォーム
	 * @param result 入力チェック結果
	 * @return 入力エラーあり: "client/user/address/update_input"
	 *         なし: "client/user/address/update_check"
	 */
	@RequestMapping(path = "/client/delivery_address/update/check", method = RequestMethod.POST)
	public String updateCheck(@Valid @ModelAttribute DeliveryAddressForm form, BindingResult result) {
		if (result.hasErrors()) {
			return "client/user/address/update_input";
		}

		session.setAttribute("deliveryAddressForm", form);
		return "client/user/address/update_check";
	}

	/**
	 * 編集を完了します。
	 *
	 * @return "client/user/address/update_complete" 編集完了画面
	 */
	@RequestMapping(path = "/client/delivery_address/update/complete", method = RequestMethod.POST)
	public String updateComplete() {
		UserBean loginUser = (UserBean) session.getAttribute("user");
		DeliveryAddressForm form = (DeliveryAddressForm) session.getAttribute("deliveryAddressForm");

		if (loginUser == null || form == null || form.getId() == null) {
			return "redirect:/syserror";
		}

		Optional<DeliveryAddress> opt = deliveryAddressRepository.findByIdAndUserId(form.getId(), loginUser.getId());
		if (opt.isEmpty()) {
			return "redirect:/client/user/detail";
		}

		DeliveryAddress deliveryAddress = opt.get();
		BeanUtils.copyProperties(form, deliveryAddress);
		deliveryAddressRepository.save(deliveryAddress);

		session.removeAttribute("deliveryAddressForm");
		return "client/user/address/update_complete";
	}

	/**
	 * 削除確認画面を表示します。
	 *
	 * @param id お届け先ID
	 * @param model Viewとの値受渡し
	 * @return "client/user/address/delete_check" 削除確認画面
	 */
	@RequestMapping(path = "/client/delivery_address/delete/check/{id}", method = RequestMethod.POST)
	public String deleteCheck(@PathVariable Integer id, Model model) {
		UserBean loginUser = (UserBean) session.getAttribute("user");
		if (loginUser == null) {
			return "redirect:/login";
		}

		Optional<DeliveryAddress> opt = deliveryAddressRepository.findByIdAndUserId(id, loginUser.getId());
		if (opt.isEmpty()) {
			return "redirect:/client/user/detail";
		}

		model.addAttribute("deliveryAddress", opt.get());
		return "client/user/address/delete_check";
	}

	/**
	 * 削除を完了します。
	 *
	 * @param id お届け先ID
	 * @return "client/user/address/delete_complete" 削除完了画面
	 */
	@RequestMapping(path = "/client/delivery_address/delete/complete/{id}", method = RequestMethod.POST)
	public String deleteComplete(@PathVariable Integer id) {
		UserBean loginUser = (UserBean) session.getAttribute("user");
		if (loginUser == null) {
			return "redirect:/login";
		}

		Optional<DeliveryAddress> opt = deliveryAddressRepository.findByIdAndUserId(id, loginUser.getId());
		if (opt.isEmpty()) {
			return "redirect:/client/user/detail";
		}

		deliveryAddressRepository.delete(opt.get());
		return "client/user/address/delete_complete";
	}

	/**
	 * お届け先管理機能の各画面から戻ります。
	 *
	 * @return 会員詳細画面
	 */
	@RequestMapping(path = "/client/delivery_address/back", method = RequestMethod.POST)
	public String back() {
		session.removeAttribute("deliveryAddressForm");
		return "redirect:/client/user/detail";
	}
}
