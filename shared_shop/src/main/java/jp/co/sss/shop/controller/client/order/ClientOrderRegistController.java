package jp.co.sss.shop.controller.client.order;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Optional;

import jp.co.sss.shop.bean.BasketBean;
import jp.co.sss.shop.bean.OrderItemBean;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.bean.UserCouponBean;
import jp.co.sss.shop.entity.CouponType;
import jp.co.sss.shop.entity.DeliveryAddress;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.entity.Order;
import jp.co.sss.shop.entity.OrderItem;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.entity.UserCoupon;
import jp.co.sss.shop.form.OrderForm;
import jp.co.sss.shop.repository.DeliveryAddressRepository;
import jp.co.sss.shop.repository.ItemRepository;
import jp.co.sss.shop.repository.OrderItemRepository;
import jp.co.sss.shop.repository.OrderRepository;
import jp.co.sss.shop.repository.UserRepository;
import jp.co.sss.shop.repository.UserCouponRepository;
import jp.co.sss.shop.service.BeanTools;
import jp.co.sss.shop.service.PriceCalc;
import jp.co.sss.shop.util.Constant;

/**
 * 注文登録機能(一般会員用)のコントローラクラスです。
 *
 * @author SystemShared
 */
@Controller
public class ClientOrderRegistController {

	/**
	 * 買い物かご情報のセッション属性名
	 */
	private static final String BASKET_BEANS = "basketBeans";

	/**
	 * 注文入力フォームのセッション属性名
	 */
	private static final String ORDER_FORM = "orderForm";

	private static final String ORDER_ADDRESS_ERROR = "orderAddressError";

	private static final int DELIVERY_ADDRESS_LIMIT = 3;

	private static final String[] ORDER_FORM_FIELD_ORDER = {
			"postalCode", "address", "name", "phoneNumber", "deliveryDate"
	};

	/**
	 * 在庫不足商品名リストの属性名
	 */
	private static final String ITEM_NAME_LIST_LESS_THAN = "itemNameListLessThan";

	/**
	 * 在庫切れ商品名リストの属性名
	 */
	private static final String ITEM_NAME_LIST_ZERO = "itemNameListZero";

	private static final int ACTIVE_COUPON_TYPE = 1;
	private static final ZoneId JAPAN_ZONE = ZoneId.of("Asia/Tokyo");
	private static final DateTimeFormatter COUPON_DATE_FORMAT =
			DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

	/**
	 * 商品情報リポジトリ
	 */
	@Autowired
	ItemRepository itemRepository;

	/**
	 * 注文情報リポジトリ
	 */
	@Autowired
	OrderRepository orderRepository;

	/**
	 * 注文商品情報リポジトリ
	 */
	@Autowired
	OrderItemRepository orderItemRepository;

	/**
	 * 会員情報リポジトリ
	 */
	@Autowired
	UserRepository userRepository;

	/**
	 * 会員保有クーポンリポジトリ
	 */
	@Autowired
	UserCouponRepository userCouponRepository;

	/**
	 * お届け先情報リポジトリ
	 */
	@Autowired
	DeliveryAddressRepository deliveryAddressRepository;

	/**
	 * Entity、Form、Bean間のデータコピーサービス
	 */
	@Autowired
	BeanTools beanTools;

	/**
	 * 金額計算サービス
	 */
	@Autowired
	PriceCalc priceCalc;

	/**
	 * メッセージソース
	 */
	@Autowired
	org.springframework.context.MessageSource messageSource;

	/**
	 * セッション
	 */
	@Autowired
	HttpSession session;

	/**
	 * 注文手続き開始時に届け先入力フォームを初期化します。
	 * 
	 * @author シュエ ジーハン
	 * @return "redirect:/client/order/address/input" 届け先入力画面表示処理へリダイレクト
	 */
	@RequestMapping(path = "/client/order/address/input", method = RequestMethod.POST)
	public String addressInputInit() {
		if (isBasketEmpty()) {
			return "redirect:/client/basket/list";
		}

		UserBean loginUser = (UserBean) session.getAttribute("user");
		if (loginUser == null) {
			return "redirect:/login";
		}

		// 登録済みのお届け先を確認
		List<DeliveryAddress> addresses = deliveryAddressRepository.findByUserIdOrderByAddressNo(loginUser.getId());

		// お届け先が登録されている場合は、お届け先選択画面へ遷移する
		if (!addresses.isEmpty()) {
			OrderForm orderForm = new OrderForm();
			orderForm.setId(loginUser.getId());
			orderForm.setPayMethod(Constant.DEFAULT_PAYMENT_METHOD);

			// 初期選択は「お届け先1」
			DeliveryAddress address1 = null;
			for (DeliveryAddress addr : addresses) {
				if (addr.getAddressNo() == 1) {
					address1 = addr;
					break;
				}
			}
			if (address1 != null) {
				orderForm.setDeliveryAddressId(address1.getId());
				BeanUtils.copyProperties(address1, orderForm);
				orderForm.setId(loginUser.getId());
			} else {
				// お届け先1がない場合は、リストの最初の要素を選択
				DeliveryAddress firstAddress = addresses.get(0);
				orderForm.setDeliveryAddressId(firstAddress.getId());
				BeanUtils.copyProperties(firstAddress, orderForm);
				orderForm.setId(loginUser.getId());
			}

			session.setAttribute(ORDER_FORM, orderForm);
			return "redirect:/client/order/address/select";
		}

		// お届け先が登録されていない場合は、従来通り新規入力画面の初期化を行う
		User user = userRepository.findByIdAndDeleteFlag(loginUser.getId(), Constant.NOT_DELETED);
		if (user == null) {
			return "redirect:/syserror";
		}

		OrderForm orderForm = new OrderForm();
		BeanUtils.copyProperties(user, orderForm);
		orderForm.setId(user.getId());
		orderForm.setPayMethod(Constant.DEFAULT_PAYMENT_METHOD);

		session.setAttribute(ORDER_FORM, orderForm);
		session.removeAttribute("result");
		return "redirect:/client/order/address/input";
	}


	/**
	 * 届け先選択画面を表示します。
	 *
	 * @param model Viewとの値受渡し
	 * @return "client/order/address_selection" 届け先選択画面
	 */
	@RequestMapping(path = "/client/order/address/select", method = RequestMethod.GET)
	public String addressSelect(Model model) {
		UserBean loginUser = (UserBean) session.getAttribute("user");
		OrderForm orderForm = (OrderForm) session.getAttribute(ORDER_FORM);
		if (loginUser == null || orderForm == null) {
			return "redirect:/syserror";
		}

		List<DeliveryAddress> addresses = deliveryAddressRepository.findByUserIdOrderByAddressNo(loginUser.getId());
		model.addAttribute("deliveryAddresses", addresses);
		model.addAttribute("canAddDeliveryAddress", addresses.size() < DELIVERY_ADDRESS_LIMIT);
		restoreOrderFormBindingResult(model);
		String orderAddressError = (String) session.getAttribute(ORDER_ADDRESS_ERROR);
		if (orderAddressError != null) {
			model.addAttribute(ORDER_ADDRESS_ERROR, orderAddressError);
			session.removeAttribute(ORDER_ADDRESS_ERROR);
		}
		model.addAttribute(ORDER_FORM, orderForm);
		return "client/order/address_selection";
	}

	/**
	 * 届け先選択値を保存し、支払方法選択画面へ遷移します。
	 *
	 * @param deliveryAddressId 選択されたお届け先ID
	 * @return 支払方法選択画面表示処理へリダイレクト
	 */
	@RequestMapping(path = "/client/order/address/select", method = RequestMethod.POST)
	public String addressSelectCheck(@RequestParam Integer deliveryAddressId, @ModelAttribute OrderForm form) {
		UserBean loginUser = (UserBean) session.getAttribute("user");
		OrderForm orderForm = (OrderForm) session.getAttribute(ORDER_FORM);
		if (loginUser == null || orderForm == null) {
			return "redirect:/syserror";
		}

		Optional<DeliveryAddress> opt = deliveryAddressRepository.findByIdAndUserId(deliveryAddressId, loginUser.getId());
		if (opt.isEmpty()) {
			return "redirect:/syserror";
		}

		orderForm.setDeliveryAddressId(deliveryAddressId);
		orderForm.setDeliveryDate(form.getDeliveryDate());
		BindingResult result = new BeanPropertyBindingResult(orderForm, ORDER_FORM);
		validateDeliveryDate(orderForm, result);
		if (result.hasErrors()) {
			clearInvalidAddressFields(orderForm, result);
			session.setAttribute(ORDER_FORM, orderForm);
			session.setAttribute("result", createClearedRejectedValueResult(orderForm, result));
			return "redirect:/client/order/address/select";
		}

		DeliveryAddress address = opt.get();
		copyDeliveryAddressToOrderForm(address, orderForm, loginUser.getId());

		session.setAttribute(ORDER_FORM, orderForm);
		return "redirect:/client/order/payment/input";
	}

	/**
	 * 届け先入力画面を表示します。
	 *
	 * @author シュエ ジーハン
	 * @param model Viewとの値受渡し
	 * @return "client/order/address_input" 届け先入力画面
	 * @throws RuntimeException 注文情報がセッションに存在しない場合
	 */
	@RequestMapping(path = "/client/order/address/input", method = RequestMethod.GET)
	public String addressInput(Model model) {
		UserBean loginUser = (UserBean) session.getAttribute("user");
		OrderForm orderForm = (OrderForm) session.getAttribute(ORDER_FORM);
		if (loginUser == null || orderForm == null) {
			return "redirect:/syserror";
		}
		long addressCount = deliveryAddressRepository.countByUserId(loginUser.getId());
		if (addressCount >= DELIVERY_ADDRESS_LIMIT) {
			session.setAttribute(ORDER_ADDRESS_ERROR, "お届け先は最大3件まで登録できます。登録済みのお届け先を選択してください。");
			return "redirect:/client/order/address/select";
		}

		// POSTの入力チェックでエラーが発生した場合、PRG(Post-Redirect-Get)形式でこのGETメソッドへ戻ってくる。
		// Redirectを挟むと通常のリクエストスコープのBindingResultは消えてしまうため、
		// addressInputCheckメソッド側で一時的にセッションへ退避したエラー情報をここで取り出す。
		if (!restoreOrderFormBindingResult(model)) {
			User user = userRepository.findByIdAndDeleteFlag(loginUser.getId(), Constant.NOT_DELETED);
			if (user == null) {
				return "redirect:/syserror";
			}
			OrderForm newAddressForm = new OrderForm();
			BeanUtils.copyProperties(user, newAddressForm);
			newAddressForm.setId(loginUser.getId());
			newAddressForm.setPayMethod(orderForm.getPayMethod());
			newAddressForm.setDeliveryDate(orderForm.getDeliveryDate());
			orderForm = newAddressForm;
			session.setAttribute(ORDER_FORM, orderForm);
		}

		// 画面の入力欄に現在の注文フォーム情報を表示するため、OrderFormをModelへ設定する。
		model.addAttribute("isAddressEdit", false);
		model.addAttribute(ORDER_FORM, orderForm);
		return "client/order/address_input";
	}

	@RequestMapping(path = "/client/order/address/update/input", method = RequestMethod.GET)
	public String addressUpdateInput(@RequestParam Integer deliveryAddressId, Model model) {
		UserBean loginUser = (UserBean) session.getAttribute("user");
		OrderForm currentOrderForm = (OrderForm) session.getAttribute(ORDER_FORM);
		if (loginUser == null || currentOrderForm == null) {
			return "redirect:/syserror";
		}

		Optional<DeliveryAddress> opt = deliveryAddressRepository.findByIdAndUserId(deliveryAddressId, loginUser.getId());
		if (opt.isEmpty()) {
			return "redirect:/syserror";
		}

		OrderForm editForm = new OrderForm();
		editForm.setPayMethod(currentOrderForm.getPayMethod());
		editForm.setDeliveryDate(currentOrderForm.getDeliveryDate());
		copyDeliveryAddressToOrderForm(opt.get(), editForm, loginUser.getId());
		model.addAttribute("isAddressEdit", true);
		model.addAttribute(ORDER_FORM, editForm);
		return "client/order/address_input";
	}

	/**
	 * 届け先入力値をチェックし、保存済み届け先へ追加します。
	 *
	 * @author シュエ ジーハン
	 * @param form 注文入力フォーム
	 * @param result 入力チェック結果
	 * @return 入力エラーあり: "client/order/address/input"
	 * 					なし: "redirect:/client/order/address/select"
	 */
	@RequestMapping(path = "/client/order/address/regist", method = RequestMethod.POST)
	public String addressInputCheck(@Valid @ModelAttribute OrderForm form, BindingResult result, Model model) {
		UserBean loginUser = (UserBean) session.getAttribute("user");
		OrderForm lastOrderForm = (OrderForm) session.getAttribute(ORDER_FORM);
		if (loginUser == null || lastOrderForm == null) {
			return "redirect:/syserror";
		}
		
		// 画面からPOSTされる届け先入力フォームには、入力項目として表示されていない値が含まれない場合がある。
		// 特に会員IDや支払方法は、注文登録・確認の後続処理で必要になるため、
		// POSTされたformに値が入っていない場合は、セッションに保存していた前回のOrderFormから補完する。
		if (form.getId() == null) {
			form.setId(lastOrderForm.getId());
		}
		if (form.getPayMethod() == null) {
			form.setPayMethod(lastOrderForm.getPayMethod());
		}

		if (result.hasErrors()) {
			clearInvalidAddressFields(form, result);
			model.addAttribute("isAddressEdit", false);
			model.addAttribute(ORDER_FORM, form);
			return "client/order/address_input";
		}

		if (deliveryAddressRepository.countByUserId(loginUser.getId()) >= DELIVERY_ADDRESS_LIMIT) {
			session.setAttribute(ORDER_ADDRESS_ERROR, "お届け先は最大3件まで登録できます。登録済みのお届け先を選択してください。");
			return "redirect:/client/order/address/select";
		}

		User user = userRepository.findByIdAndDeleteFlag(loginUser.getId(), Constant.NOT_DELETED);
		if (user == null) {
			return "redirect:/syserror";
		}
		DeliveryAddress address = new DeliveryAddress();
		address.setUser(user);
		address.setAddressNo(nextAddressNo(loginUser.getId()));
		copyOrderFormToDeliveryAddress(form, address);
		deliveryAddressRepository.save(address);

		copyDeliveryAddressToOrderForm(address, lastOrderForm, loginUser.getId());
		session.setAttribute(ORDER_FORM, lastOrderForm);
		return "redirect:/client/order/address/select";
	}

	@RequestMapping(path = "/client/order/address/update", method = RequestMethod.POST)
	public String addressUpdateCheck(@Valid @ModelAttribute OrderForm form, BindingResult result, Model model) {
		UserBean loginUser = (UserBean) session.getAttribute("user");
		OrderForm currentOrderForm = (OrderForm) session.getAttribute(ORDER_FORM);
		if (loginUser == null || currentOrderForm == null || form.getDeliveryAddressId() == null) {
			return "redirect:/syserror";
		}

		if (result.hasErrors()) {
			clearInvalidAddressFields(form, result);
			model.addAttribute("isAddressEdit", true);
			model.addAttribute(ORDER_FORM, form);
			return "client/order/address_input";
		}

		Optional<DeliveryAddress> opt = deliveryAddressRepository.findByIdAndUserId(
				form.getDeliveryAddressId(), loginUser.getId());
		if (opt.isEmpty()) {
			return "redirect:/syserror";
		}
		DeliveryAddress address = opt.get();
		copyOrderFormToDeliveryAddress(form, address);
		deliveryAddressRepository.save(address);

		copyDeliveryAddressToOrderForm(address, currentOrderForm, loginUser.getId());
		session.setAttribute(ORDER_FORM, currentOrderForm);
		return "redirect:/client/order/address/select";
	}

	/**
	 * 支払方法選択画面を表示します。
	 *
	 * @author シュエ ジーハン
	 * @param model Viewとの値受渡し
	 * @return "client/order/payment_input" 支払方法選択画面
	 * @throws RuntimeException 注文情報がセッションに存在しない場合
	 */
	@Transactional
	@RequestMapping(path = "/client/order/payment/input", method = RequestMethod.GET)
	public String paymentInput(Model model) {
		OrderForm orderForm = (OrderForm) session.getAttribute(ORDER_FORM);
		if (orderForm == null) {
			return "redirect:/syserror";
		}

		// 支払方法選択画面で、現在セッションに保持されている注文情報を利用できるようModelへ設定する。
		model.addAttribute(ORDER_FORM, orderForm);

		// 画面側で現在選択中の支払方法を初期選択状態にするため、payMethodを個別にModelへ渡す。
		// 住所入力から初めて遷移した場合はデフォルト支払方法、
		// 戻る操作などで再表示された場合は前回選択された支払方法が表示される。
		model.addAttribute("payMethod", orderForm.getPayMethod());

		UserBean loginUser = (UserBean) session.getAttribute("user");
		if (loginUser == null) {
			return "redirect:/login";
		}

		Timestamp now = currentTimestamp();
		userCouponRepository.deleteExpired(now);
		Integer total = calculateCurrentBasketTotal();
		List<UserCoupon> coupons = userCouponRepository.findAvailableByUserId(loginUser.getId(), now);
		model.addAttribute("total", total);
		model.addAttribute("couponBeans", toCouponBeans(coupons, total));
		model.addAttribute("selectedCouponId", orderForm.getCouponId());
		model.addAttribute("couponDiscountAmount", safeDiscountAmount(orderForm));
		model.addAttribute("discountedTotal", Math.max(0, total - safeDiscountAmount(orderForm)));
		Object couponError = session.getAttribute("couponError");
		if (couponError != null) {
			model.addAttribute("couponError", couponError);
			session.removeAttribute("couponError");
		}

		// 会員の最新情報を取得して保有ポイントをモデルに追加
		User user = userRepository.findByIdAndDeleteFlag(loginUser.getId(), Constant.NOT_DELETED);
		model.addAttribute("point", user.getPoint());
		model.addAttribute("usePoint", orderForm.getUsePoint());
		model.addAttribute("discountedTotal", Math.max(0, total - safeDiscountAmount(orderForm)));
		Object pointError = session.getAttribute("pointError");
		if (pointError != null) {
			model.addAttribute("pointError", pointError);
			session.removeAttribute("pointError");
		}

		return "client/order/payment_input";
	}

	/**
	 * 支払方法を保存し、注文確認画面へ遷移します。
	 *
	 * @author 秋葉 真穂
	 * @param payMethod 支払方法
	 * @return "redirect:/client/order/check" 
	 * 			注文確認画面表示処理へリダイレクト
	 * @throws RuntimeException セッション情報が取得できない場合
	 */
	@RequestMapping(path = "/client/order/check", method = RequestMethod.POST)
	public String orderCheck(
			@RequestParam Integer payMethod,
			@RequestParam(required = false) Integer couponId,
			@RequestParam(required = false) Integer usePoint) {
		
		// 選択された支払方法を注文入力フォームへ設定し、セッションへ保存する。
		// 1. セッションからORDER_FORMキーでOrderFormを取得する。
		OrderForm orderForm = (OrderForm) session.getAttribute(ORDER_FORM);
		
		// 2. OrderFormがnullの場合、注文手続きの途中情報が失われているため、システムエラー画面へリダイレクトする。
		if (orderForm == null) {
	        return "redirect:/syserror";
	    }
		
		// 3. 取得できたOrderFormに、画面で選択されたpayMethodをsetPayMethodで設定する。
		if (payMethod == null || payMethod < 1 || payMethod > 5) {
			return "redirect:/syserror";
		}
		orderForm.setPayMethod(payMethod);

		UserBean loginUser = (UserBean) session.getAttribute("user");
		if (loginUser == null) {
			return "redirect:/login";
		}
		Integer total = calculateCurrentBasketTotal();
		if (!setSelectedCoupon(orderForm, couponId, loginUser.getId(), total)) {
			session.setAttribute("couponError", "選択したクーポンは利用できません。利用条件をご確認ください。");
			return "redirect:/client/order/payment/input";
		}

		// ポイントバリデーション
		if (usePoint == null) {
			usePoint = 0;
		}
		if (usePoint < 0) {
			session.setAttribute("pointError", messageSource.getMessage("msg.point.invalid.input", null, java.util.Locale.JAPAN));
			return "redirect:/client/order/payment/input";
		}
		if (usePoint > 0) {
			User user = userRepository.findByIdAndDeleteFlag(loginUser.getId(), Constant.NOT_DELETED);
			int holdPoint = user.getPoint();
			int discountedTotal = total - safeDiscountAmount(orderForm);

			if (holdPoint < 100 || usePoint < 100) {
				session.setAttribute("pointError", messageSource.getMessage("msg.point.minimum.requirement", null, java.util.Locale.JAPAN));
				return "redirect:/client/order/payment/input";
			}
			if (usePoint > holdPoint) {
				session.setAttribute("pointError", messageSource.getMessage("msg.point.over.hold", null, java.util.Locale.JAPAN));
				return "redirect:/client/order/payment/input";
			}
			if (usePoint > discountedTotal) {
				session.setAttribute("pointError", messageSource.getMessage("msg.point.over.price", null, java.util.Locale.JAPAN));
				return "redirect:/client/order/payment/input";
			}
		}
		orderForm.setUsePoint(usePoint);
		
		// 4. 更新後のOrderFormを再度セッションへ保存する。
		session.setAttribute(ORDER_FORM, orderForm);
		
		// 5. 注文確認画面表示用のGETメソッドへリダイレクトする。
		return "redirect:/client/order/check";
	}

	/**
	 * 既存のController単体テストとの互換性を保つためのクーポン・ポイント未使用呼び出しです。
	 */
	public String orderCheck(Integer payMethod) {
		return orderCheck(payMethod, null, 0);
	}

	/**
	 * 注文確認画面を表示します。
	 *
	 * @author 秋葉 真穂
	 * @param model Viewとの値受渡し
	 * @return "client/order/check" 注文確認画面
	 * @see #createOrderItemBeansForCheck(Model)
	 */
	@RequestMapping(path = "/client/order/check", method = RequestMethod.GET)
	public String orderCheckView(Model model) {
		
		// 買い物かご商品の在庫確認、注文商品Bean生成、合計金額計算を行い画面へ渡す。
		// 実装手順参考メモ:
		// 1. セッションからORDER_FORMキーでOrderFormを取得する。
		OrderForm orderForm = (OrderForm) session.getAttribute(ORDER_FORM);
		
		// 2. OrderFormがnullの場合、注文者情報や届け先情報が確認できないため、システムエラー画面へリダイレクトする。
		if (orderForm == null) {
			return "redirect:/syserror";
			}
		
		// 3. createOrderItemBeansForCheck(model)を呼び出し、買い物かご情報をもとに注文確認画面用のOrderItemBeanリストを生成する。
		// このメソッド内で、在庫切れ商品・在庫不足商品の判定と、買い物かご数量の補正も行われる。
		List<OrderItemBean> orderItemBeans = createOrderItemBeansForCheck(model);
		
		// 注文時点で注文商品すべての在庫が0の場合の考慮
		if (orderItemBeans == null || orderItemBeans.isEmpty()) {
		    model.addAttribute("orderItemBeans", null);
		    return "client/order/check";
		}
		
		// 4. OrderFormをModelへ追加し、画面で届け先情報・支払方法を表示できるようにする。
		model.addAttribute(ORDER_FORM, orderForm);
		
		// 5. OrderItemBeanリストがnullではなく空でもない場合、priceCalcで小計込みの合計金額を計算する。
		if (orderItemBeans != null && !orderItemBeans.isEmpty()) {
			Integer total = priceCalc.orderItemBeanPriceTotalUseSubtotal(orderItemBeans);
			UserBean loginUser = (UserBean) session.getAttribute("user");
			if (loginUser == null) {
				return "redirect:/login";
			}
			if (!setSelectedCoupon(orderForm, orderForm.getCouponId(), loginUser.getId(), total)) {
				session.setAttribute("couponError",
						"注文内容の変更により、選択したクーポンが利用できなくなりました。");
				return "redirect:/client/order/payment/input";
			}
			
			// totalをModelへ追加
			model.addAttribute("total", total);
			model.addAttribute("couponDiscountAmount", safeDiscountAmount(orderForm));
			int totalAfterCoupon = Math.max(0, total - safeDiscountAmount(orderForm));
			model.addAttribute("discountedTotal", totalAfterCoupon);

			// ポイント関連の計算
			int usePoint = orderForm.getUsePoint() != null ? orderForm.getUsePoint() : 0;
			int totalAfterPoint = Math.max(0, totalAfterCoupon - usePoint);
			int earnedPoint = (int) Math.floor(totalAfterPoint * 0.01);

			model.addAttribute("usePoint", usePoint);
			model.addAttribute("totalAfterPoint", totalAfterPoint);
			model.addAttribute("earnedPoint", earnedPoint);
			}
		
		// 6. orderItemBeansをModelへ追加し、注文確認画面へ渡す。
		model.addAttribute("orderItemBeans", orderItemBeans);
		
		// 7. 最後に注文確認画面のView名を返す。
		return "client/order/check";
	}

	/**
	 * 注文確認画面または支払方法選択画面から前画面へ戻ります。
	 *
	 * @author 秋葉 真穂
	 * @return "redirect:/client/order/address/input" 届け先入力画面表示処理へリダイレクト
	 */
	@RequestMapping(path = "/client/order/payment/back", method = RequestMethod.POST)
	public String paymentBack() {
		
		return "redirect:/client/order/address/select";
	}

	@RequestMapping(path = "/client/order/check/back", method = RequestMethod.POST)
	public String checkBack() {
		return "redirect:/client/order/payment/input";
	}

	/**
	 * 注文を確定します。
	 * 
	 * @author 秋葉 真穂
	 * @return 在庫エラーあり:
	 *         "redirect:/client/order/check"
	 *         在庫エラーなし:
	 *         "redirect:/client/order/complete"
	 * @throws RuntimeException
	 *         注文登録処理中にDB更新エラーが発生した場合
	 * @see #createOrder(OrderForm)
	 * @see #canOrder(List)
	 */
	@Transactional
	@RequestMapping(path = "/client/order/complete", method = RequestMethod.POST)
	public String orderComplete() {
		
		// 注文確定直前の在庫確認、注文/注文商品登録、セッション情報削除を行う。
		// 注意点: OrderFormのidは会員IDのため、Orderエンティティのidへコピーしないこと。

		// 1. セッションからOrderFormと買い物かご情報(BasketBeanリスト)を取得する。
		OrderForm orderForm = (OrderForm) session.getAttribute(ORDER_FORM);
		List<BasketBean> basketBeans = getBasketBeans();
		
		// 2. OrderFormがnull、買い物かごがnull、または空の場合は、注文に必要な情報が不足しているためシステムエラーへ遷移する。
		if (orderForm == null || basketBeans == null || basketBeans.isEmpty()) {
			return "redirect:/syserror";
		}
		
		// 3. canOrder(basketBeans)を呼び出し、注文確定直前の最新在庫で本当に注文可能か確認する。
		if (!canOrder(basketBeans)) {
			
			// 4. 注文不可の場合は注文確認画面へ戻す
			return "redirect:/client/order/check";
		}

		UserBean loginUser = (UserBean) session.getAttribute("user");
		if (loginUser == null || !loginUser.getId().equals(orderForm.getId())) {
			return "redirect:/login";
		}

		Integer latestTotal = calculateCurrentBasketTotal();
		UserCoupon selectedCoupon = null;
		if (orderForm.getCouponId() != null) {
			selectedCoupon = userCouponRepository.findByIdAndUserIdForUpdate(
					orderForm.getCouponId(), loginUser.getId());
			if (!isCouponUsable(selectedCoupon, latestTotal, currentTimestamp())) {
				clearCoupon(orderForm);
				session.setAttribute(ORDER_FORM, orderForm);
				session.setAttribute("couponError",
						"選択したクーポンの有効期限または利用条件が変わったため、再度選択してください。");
				return "redirect:/client/order/payment/input";
			}
			applyCoupon(orderForm, selectedCoupon, latestTotal);
		}
		
		// 5. createOrder(orderForm)を呼び出し、届け先・支払方法・会員情報を持つOrder Entityを生成する。
		Order order = createOrder(orderForm);
		if (selectedCoupon != null) {
			order.setCouponType(selectedCoupon.getCouponType());
		}

		// 注文情報のポイント利用・付与履歴をセット
		int usePoint = orderForm.getUsePoint() != null ? orderForm.getUsePoint() : 0;
		int totalAfterCoupon = latestTotal - safeDiscountAmount(orderForm);
		int totalAfterPoint = Math.max(0, totalAfterCoupon - usePoint);
		int earnedPoint = (int) Math.floor(totalAfterPoint * 0.01);
		order.setUsePoint(usePoint);
		order.setEarnedPoint(earnedPoint);
		
		// 6. orderRepository.save(order)で注文情報を登録し、保存後のOrderを取得する。
		order = orderRepository.save(order);
		
		// 7. 買い物かご内の商品ごとに、商品情報をDBから取得する。
		for (BasketBean basketBean : basketBeans) {

			Item item = itemRepository.findByIdAndDeleteFlag(
					basketBean.getId(),
					Constant.NOT_DELETED);
			
			// 8. OrderItem Entityを生成し、保存済みOrder、商品、注文数、注文時点の商品単価を設定して保存する。
			OrderItem orderItem = new OrderItem();
			orderItem.setOrder(order);
			orderItem.setItem(item);
			orderItem.setQuantity(basketBean.getOrderNum());
			
			// 注文時点の価格を保存
			orderItem.setPrice(item.getPrice());
			orderItemRepository.save(orderItem);

			// 9. 注文数分だけItemの在庫数を減らし、itemRepository.save(item)で在庫を更新する。
			item.setStock(item.getStock() - basketBean.getOrderNum());
			itemRepository.save(item);
		}

		if (selectedCoupon != null) {
			userCouponRepository.delete(selectedCoupon);
		}

		// ポイント更新
		User user = userRepository.findByIdAndDeleteFlag(loginUser.getId(), Constant.NOT_DELETED);
		user.setPoint(user.getPoint() - usePoint + earnedPoint);
		userRepository.save(user);
		
		// 10. 注文登録後は、セッションからORDER_FORMとBASKET_BEANSを削除する。
		session.removeAttribute(ORDER_FORM);
		session.removeAttribute(BASKET_BEANS);
		
		// 11. 注文完了画面表示用のGETメソッドへリダイレクトする。
		// 補足: この処理は注文登録・注文商品登録・在庫更新をまとめて行うため、
		//       実装時は@Transactionalを付与すると、途中でエラーが発生した場合に一括ロールバックできる。
		return "redirect:/client/order/complete";
	}

	/**
	 * 注文完了画面を表示します。
	 *
	 * @author 秋葉 真穂
	 * @return "client/order/complete" 注文完了画面
	 */
	@RequestMapping(path = "/client/order/complete", method = RequestMethod.GET)
	public String orderCompleteFinish() {
		
		// 注文完了画面を表示するための後処理が必要な場合はここに実装する。
		return "client/order/complete";
	}

	/**
	 * 注文情報を生成します。
	 * @return 注文情報Entityクラス
	 *
	 */
	private Order createOrder(OrderForm orderForm) {
		Order order = new Order();

		// OrderFormには、届け先入力画面で入力・確認した注文者情報が保持されている。
		// ここではその内容をOrder Entityへ移し替え、DBのordersテーブルへ保存できる形にする。
		// ただし、OrderFormのidは「会員ID」として使っているため、
		// Order Entity自身のid(注文ID)へはコピーしない。
		// 注文IDはDB側のシーケンス／自動採番に任せる。
		order.setPostalCode(orderForm.getPostalCode());
		order.setAddress(orderForm.getAddress());
		order.setName(orderForm.getName());
		order.setPhoneNumber(orderForm.getPhoneNumber());
		order.setPayMethod(orderForm.getPayMethod());
		if (orderForm.getDeliveryDate() != null && !orderForm.getDeliveryDate().isEmpty()) {
			order.setDeliveryDate(java.sql.Date.valueOf(orderForm.getDeliveryDate()));
		}
		order.setCouponName(orderForm.getCouponName());
		order.setCouponDiscountRate(orderForm.getCouponDiscountRate());
		order.setCouponDiscountAmount(safeDiscountAmount(orderForm));

		// 注文者の会員情報をOrderへ紐付ける。
		// ここでは会員IDだけを持つUser Entityを作成して設定している。
		// これにより、ordersテーブルのuser_idに該当会員のIDが登録される。
		User user = new User();
		user.setId(orderForm.getId());
		order.setUser(user);
		return order;
	}

	private boolean setSelectedCoupon(
			OrderForm orderForm, Integer couponId, Integer userId, Integer total) {
		if (couponId == null) {
			clearCoupon(orderForm);
			return true;
		}
		UserCoupon coupon = userCouponRepository.findAvailableByIdAndUserId(
				couponId, userId, currentTimestamp());
		if (!isCouponUsable(coupon, total, currentTimestamp())) {
			clearCoupon(orderForm);
			return false;
		}
		applyCoupon(orderForm, coupon, total);
		return true;
	}

	private boolean isCouponUsable(UserCoupon coupon, Integer total, Timestamp now) {
		if (coupon == null || coupon.getCouponType() == null || coupon.getExpiresAt() == null) {
			return false;
		}
		CouponType type = coupon.getCouponType();
		return Integer.valueOf(ACTIVE_COUPON_TYPE).equals(type.getActiveFlag())
				&& coupon.getExpiresAt().after(now)
				&& total != null
				&& total >= type.getMinimumOrderAmount();
	}

	private void applyCoupon(OrderForm orderForm, UserCoupon coupon, Integer total) {
		CouponType type = coupon.getCouponType();
		orderForm.setCouponId(coupon.getId());
		orderForm.setCouponName(type.getName());
		orderForm.setCouponDiscountRate(type.getDiscountRate());
		orderForm.setCouponDiscountAmount(calculateCouponDiscount(total, type.getDiscountRate()));
	}

	private int calculateCouponDiscount(Integer total, Integer discountRate) {
		if (total == null || discountRate == null || total <= 0 || discountRate <= 0) {
			return 0;
		}
		return total * discountRate / 100;
	}

	private void clearCoupon(OrderForm orderForm) {
		orderForm.setCouponId(null);
		orderForm.setCouponName(null);
		orderForm.setCouponDiscountRate(null);
		orderForm.setCouponDiscountAmount(0);
	}

	private int safeDiscountAmount(OrderForm orderForm) {
		return orderForm.getCouponDiscountAmount() == null ? 0 : orderForm.getCouponDiscountAmount();
	}

	private Integer calculateCurrentBasketTotal() {
		int total = 0;
		List<BasketBean> basketBeans = getBasketBeans();
		if (basketBeans == null) {
			return total;
		}
		for (BasketBean basketBean : basketBeans) {
			Item item = itemRepository.findByIdAndDeleteFlag(basketBean.getId(), Constant.NOT_DELETED);
			if (item != null && item.getPrice() != null && basketBean.getOrderNum() != null
					&& basketBean.getOrderNum() > 0) {
				total += item.getPrice() * basketBean.getOrderNum();
			}
		}
		return total;
	}

	private List<UserCouponBean> toCouponBeans(List<UserCoupon> coupons, Integer total) {
		List<UserCouponBean> beans = new ArrayList<UserCouponBean>();
		for (UserCoupon coupon : coupons) {
			CouponType type = coupon.getCouponType();
			UserCouponBean bean = new UserCouponBean();
			bean.setId(coupon.getId());
			bean.setName(type.getName());
			bean.setDiscountRate(type.getDiscountRate());
			bean.setMinimumOrderAmount(type.getMinimumOrderAmount());
			bean.setAcquiredAt(formatCouponDate(coupon.getAcquiredAt()));
			bean.setExpiresAt(formatCouponDate(coupon.getExpiresAt()));
			boolean available = total >= type.getMinimumOrderAmount();
			bean.setAvailable(available);
			if (!available) {
				bean.setUnavailableReason("ご注文金額が最低利用金額に達していません。");
			}
			beans.add(bean);
		}
		return beans;
	}

	private String formatCouponDate(Timestamp timestamp) {
		return timestamp == null ? "" : timestamp.toLocalDateTime().format(COUPON_DATE_FORMAT);
	}

	private Timestamp currentTimestamp() {
		return Timestamp.valueOf(LocalDateTime.now(JAPAN_ZONE));
	}

	private boolean restoreOrderFormBindingResult(Model model) {
		BindingResult result = (BindingResult) session.getAttribute("result");
		if (result == null) {
			return false;
		}
		model.addAttribute("org.springframework.validation.BindingResult.orderForm", result);
		session.removeAttribute("result");
		return true;
	}

	private void validateDeliveryDate(OrderForm form, BindingResult result) {
		if (form.getDeliveryDate() == null || form.getDeliveryDate().isEmpty()) {
			return;
		}
		if (!form.getDeliveryDate().matches("^[0-9]{4}-[0-9]{2}-[0-9]{2}$")) {
			result.rejectValue("deliveryDate", "orderForm.deliveryDate.invalid_format");
			return;
		}
		try {
			java.time.LocalDate deliveryDate = java.time.LocalDate.parse(form.getDeliveryDate());
			java.time.LocalDate today = java.time.LocalDate.now();
			java.time.LocalDate minDate = today.plusDays(3);
			java.time.LocalDate maxDate = today.plusDays(14);
			if (deliveryDate.isBefore(minDate) || deliveryDate.isAfter(maxDate)) {
				result.rejectValue("deliveryDate", "orderForm.deliveryDate.invalid");
			}
		} catch (java.time.format.DateTimeParseException e) {
			result.rejectValue("deliveryDate", "orderForm.deliveryDate.invalid_format");
		}
	}

	private void copyDeliveryAddressToOrderForm(DeliveryAddress address, OrderForm orderForm, Integer userId) {
		String deliveryDate = orderForm.getDeliveryDate();
		Integer payMethod = orderForm.getPayMethod();
		Integer couponId = orderForm.getCouponId();
		Integer usePoint = orderForm.getUsePoint();
		BeanUtils.copyProperties(address, orderForm);
		orderForm.setId(userId);
		orderForm.setDeliveryAddressId(address.getId());
		orderForm.setDeliveryDate(deliveryDate);
		orderForm.setPayMethod(payMethod);
		orderForm.setCouponId(couponId);
		orderForm.setUsePoint(usePoint);
	}

	private void copyOrderFormToDeliveryAddress(OrderForm form, DeliveryAddress address) {
		address.setPostalCode(form.getPostalCode());
		address.setAddress(form.getAddress());
		address.setName(form.getName());
		address.setPhoneNumber(form.getPhoneNumber());
	}

	private Integer nextAddressNo(Integer userId) {
		List<DeliveryAddress> addresses = deliveryAddressRepository.findByUserIdOrderByAddressNo(userId);
		int maxAddressNo = 0;
		for (DeliveryAddress address : addresses) {
			if (address.getAddressNo() != null && address.getAddressNo() > maxAddressNo) {
				maxAddressNo = address.getAddressNo();
			}
		}
		return maxAddressNo + 1;
	}

	private void clearInvalidAddressFields(OrderForm form, BindingResult result) {
		Set<String> invalidFields = new HashSet<String>();
		for (FieldError fieldError : result.getFieldErrors()) {
			invalidFields.add(fieldError.getField());
		}

		if (invalidFields.contains("phoneNumber")) {
			form.setPhoneNumber("");
		}
		if (invalidFields.contains("deliveryDate")) {
			form.setDeliveryDate("");
		}
	}

	private BindingResult createClearedRejectedValueResult(OrderForm form, BindingResult result) {
		BindingResult clearedResult = new BeanPropertyBindingResult(form, result.getObjectName());
		List<ObjectError> errors = new ArrayList<ObjectError>(result.getAllErrors());
		errors.sort(Comparator.comparingInt(this::orderFormFieldOrder));
		for (ObjectError error : errors) {
			if (error instanceof FieldError fieldError) {
				clearedResult.addError(new FieldError(
						fieldError.getObjectName(),
						fieldError.getField(),
						"",
						fieldError.isBindingFailure(),
						fieldError.getCodes(),
						fieldError.getArguments(),
						fieldError.getDefaultMessage()));
			} else {
				clearedResult.addError(error);
			}
		}
		return clearedResult;
	}

	private int orderFormFieldOrder(ObjectError error) {
		if (!(error instanceof FieldError fieldError)) {
			return ORDER_FORM_FIELD_ORDER.length;
		}
		for (int i = 0; i < ORDER_FORM_FIELD_ORDER.length; i++) {
			if (ORDER_FORM_FIELD_ORDER[i].equals(fieldError.getField())) {
				return i;
			}
		}
		return ORDER_FORM_FIELD_ORDER.length;
	}

	/**
	 * 注文確認画面表示用の注文商品Beanを生成します。
	 *
	 * @param model Viewとの値受渡し
	 * @return 注文商品Beanリスト
	 */
	private List<OrderItemBean> createOrderItemBeansForCheck(Model model) {
		// 注文確認画面では、現在の買い物かご情報をもとに表示用の注文商品情報を作成する。
		// 買い物かごが存在しない、または空の場合は、表示すべき注文商品がないためnullを返す。
		List<BasketBean> basketBeans = getBasketBeans();
		if (basketBeans == null || basketBeans.isEmpty()) {
			return null;
		}

		// itemNameListLessThan: 注文数より在庫数が少なかった商品の名前を保持する。
		// itemNameListZero: 在庫切れ、または削除済みで注文対象から外す商品の名前を保持する。
		// updatedBasketBeans: 最新の在庫状況に合わせて数量補正・除外した後の買い物かご情報を保持する。
		// orderItemBeans: 注文確認画面に表示するための商品名・単価・数量・小計などを持つBeanを保持する。
		List<String> itemNameListLessThan = new ArrayList<String>();
		List<String> itemNameListZero = new ArrayList<String>();
		List<BasketBean> updatedBasketBeans = new ArrayList<BasketBean>();
		List<OrderItemBean> orderItemBeans = new ArrayList<OrderItemBean>();

		for (BasketBean basketBean : basketBeans) {
			
			// 買い物かごに入れた時点から注文確認時点までの間に、
			// 商品が削除されたり、在庫数が変わったりしている可能性がある。
			// そのため、BasketBeanの情報だけを信用せず、DBから最新の商品情報を取得して確認する。
			Item item = itemRepository.findByIdAndDeleteFlag(basketBean.getId(), Constant.NOT_DELETED);
			if (item == null || item.getStock() == null || item.getStock() == 0) {
				
				// 商品が存在しない、論理削除済み、または在庫数が0の場合は注文できない。
				// 注文確認画面でユーザーに知らせるため、対象商品名を在庫切れリストへ追加する。
				// continueにより、この商品は更新後の買い物かご・注文商品表示リストには追加しない。
				itemNameListZero.add(basketBean.getName());
				continue;
			}

			// DBから取得した最新の商品ID・商品名・在庫数と、ユーザーが買い物かごで指定した注文数を使って、
			// 確認画面用に買い物かご情報を作り直す。
			BasketBean updatedBasketBean = new BasketBean(
					item.getId(), item.getName(), item.getStock(), basketBean.getOrderNum());
			if (updatedBasketBean.getOrderNum() > item.getStock()) {
				
				// 注文数が現在在庫数を超えている場合、そのままでは注文できない。
				// 注文可能な最大数である現在在庫数まで注文数を自動補正し、
				// 注文確認画面に注意メッセージを表示するため商品名を在庫不足リストへ追加する。
				updatedBasketBean.setOrderNum(item.getStock());
				itemNameListLessThan.add(item.getName());
			}

			// 補正後も注文可能な商品だけを、更新後の買い物かご情報として保持する。
			updatedBasketBeans.add(updatedBasketBean);

			// Item EntityとBasketBeanをもとに、画面表示用のOrderItemBeanを生成する。
			// OrderItemBeanには商品名・価格・数量・小計など、注文確認画面で必要な情報がまとめられる。
			orderItemBeans.add(beanTools.generateOrderItemBean(item, updatedBasketBean));
		}

		// 在庫切れ商品の除外や在庫不足商品の数量補正を反映した買い物かご情報で、セッションを更新する。
		// これにより、確認画面以降の処理では補正後の正しい数量を利用できる。
		saveBasketBeans(updatedBasketBeans);

		// 在庫不足で数量を補正した商品がある場合、画面にメッセージ表示できるようModelへ渡す。
		if (!itemNameListLessThan.isEmpty()) {
			model.addAttribute(ITEM_NAME_LIST_LESS_THAN, itemNameListLessThan);
		}

		// 在庫切れ・削除済みで買い物かごから除外した商品がある場合、画面にメッセージ表示できるようModelへ渡す。
		if (!itemNameListZero.isEmpty()) {
			model.addAttribute(ITEM_NAME_LIST_ZERO, itemNameListZero);
		}
		return orderItemBeans;
	}

	/**
	 * 注文確定可能な在庫数であるかを判定します。
	 *
	 * @param basketBeans 買い物かご情報
	 * @return true: 注文可能、false: 注文不可
	 */
	private boolean canOrder(List<BasketBean> basketBeans) {
		for (BasketBean basketBean : basketBeans) {
			
			// 注文確認画面を表示した後、注文確定ボタンを押すまでの短い間にも、
			// 他ユーザーの注文などによって在庫数が変わる可能性がある。
			// そのため、注文確定直前にもDBから最新の商品情報を取得して再確認する。
			Item item = itemRepository.findByIdAndDeleteFlag(basketBean.getId(), Constant.NOT_DELETED);
			if (basketBean.getOrderNum() == null || basketBean.getOrderNum() <= 0
					|| item == null || item.getStock() == null || item.getStock() < basketBean.getOrderNum()) {
				
				// 注文数が不正、商品が存在しない、在庫数が取得できない、または在庫数が注文数より少ない場合は注文不可。
				return false;
			}
		}
		return true;
	}

	/**
	 * 買い物かごが空であるかを判定します。
	 *
	 * @return true: 空、false: 商品あり
	 */
	private boolean isBasketEmpty() {
		
		// 買い物かご情報はセッションに保存されているため、共通メソッドから取得する。
		// nullまたは空リストの場合は、注文手続きを開始できる商品が存在しないと判定する。
		List<BasketBean> basketBeans = getBasketBeans();
		return basketBeans == null || basketBeans.isEmpty();
	}

	/**
	 * セッションから買い物かご情報を取得します。
	 *
	 * @return 買い物かご情報
	 */
	@SuppressWarnings("unchecked")
	private List<BasketBean> getBasketBeans() {
		
		// HttpSessionから買い物かご情報を取得する。
		// session.getAttributeの戻り値はObject型のため、BasketBeanのListとしてキャストする。
		// 呼び出し元ではnullの可能性も考慮して判定する。
		return (List<BasketBean>) session.getAttribute(BASKET_BEANS);
	}

	/**
	 * 買い物かご情報をセッションへ保存します。
	 *
	 * @param basketBeans 買い物かご情報
	 */
	private void saveBasketBeans(List<BasketBean> basketBeans) {
		
		// 保存対象がnullまたは空の場合は、買い物かごに商品が存在しない状態とみなし、
		// セッションから買い物かご情報自体を削除する。
		if (basketBeans == null || basketBeans.isEmpty()) {
			session.removeAttribute(BASKET_BEANS);
		} else {
			
			// 注文可能な商品が残っている場合は、最新状態の買い物かご情報としてセッションへ保存する。
			session.setAttribute(BASKET_BEANS, basketBeans);
		}
	}

}
