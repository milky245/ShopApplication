package jp.co.sss.shop.controller.client.order;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.bean.OrderBean;
import jp.co.sss.shop.bean.OrderItemBean;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.Order;
import jp.co.sss.shop.entity.OrderItem;
import jp.co.sss.shop.repository.ItemRepository;
import jp.co.sss.shop.repository.OrderRepository;
import jp.co.sss.shop.service.BeanTools;
import jp.co.sss.shop.service.PriceCalc;

/**
 * 注文表示機能(一般会員用)のコントローラクラスです。
 *
 * @author SystemShared
 */
@Controller
public class ClientOrderShowController {

	private static final Integer ORDER_CANCELED = 1;

	/**
	 * 注文情報リポジトリ
	 */
	@Autowired
	OrderRepository orderRepository;

	/**
	 * 商品情報リポジトリ
	 */
	@Autowired
	ItemRepository itemRepository;

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
	 * セッション
	 */
	@Autowired
	HttpSession session;

	/**
	 * ログイン会員の注文一覧画面を表示します。
	 *
	 * @author 秋葉 真穂
	 * @param model Viewとの値受渡し
	 * @return "client/order/list" 注文一覧画面
	 */
	@RequestMapping(path = "/client/order/list", method = { RequestMethod.GET, RequestMethod.POST })
	public String showOrderList(Model model) {

		// ログイン会員IDを条件に注文一覧を取得し、注文Beanリストを画面へ渡す。
		// ログイン会員取得
		UserBean user = (UserBean) session.getAttribute("user");

		// 注文情報を注文日時の新しい順に取得
		List<Order> orders = orderRepository.findByUserIdOrderByInsertDateDescIdDesc(user.getId());

		// Entityから画面表示用のBeanリストへ変換
		List<OrderBean> orderBeans = new ArrayList<>();
		for (Order order : orders) {

			// BeanTools共通クラスを使って一括コピー
			OrderBean bean = beanTools.copyEntityToOrderBean(order);

			// PriceCalc共通クラスを使って合計金額を計算する
			int total = priceCalc.orderItemPriceTotal(order.getOrderItemsList());
			int couponDiscount = order.getCouponDiscountAmount() == null ? 0 : order.getCouponDiscountAmount();
			int pointDiscount = order.getUsePoint() == null ? 0 : order.getUsePoint();
			bean.setTotal(Math.max(0, total - couponDiscount - pointDiscount));

			orderBeans.add(bean);
		}
		// リクエストスコープに保存
		model.addAttribute("orders", orderBeans);
		return "client/order/list";
	}

	/**
	 * ログイン会員の注文詳細画面を表示します。
	 *
	 * @author 秋葉 真穂
	 * @param id 注文ID
	 * @param model Viewとの値受渡し
	 * @return "client/order/detail" 注文詳細画面
	 */
	@RequestMapping(path = "/client/order/detail/{id}", method = { RequestMethod.GET, RequestMethod.POST })
	public String showOrder(@PathVariable Integer id, Model model) {

		// 秋葉 真穂担当: ログイン会員の注文であることを確認し、注文詳細と注文商品Beanリストを画面へ渡す。
		// ログイン会員の注文であることを確認し、注文詳細と注文商品Beanリストを画面へ渡す。
		// ログイン会員取得
		UserBean user = (UserBean) session.getAttribute("user");

		// 該当する注文情報の詳細を取得
		Order order = orderRepository.findByIdAndUserId(id, user.getId());
		if (order == null) {
			return "redirect:/syserror";
		}

		// BeanTools共通クラスを使って一括コピー
		OrderBean bean = beanTools.copyEntityToOrderBean(order);

		// 該当する注文商品Beanリストを取得
		List<OrderItemBean> orderItemBeans = beanTools.generateOrderItemBeanList(order.getOrderItemsList());

		// 注文時の商品単価から合計金額を算出し表示
		// PriceCalc共通クラスを使って合計金額を計算する
		int total = priceCalc.orderItemBeanPriceTotalUseSubtotal(orderItemBeans);
		bean.setTotal(total);

		// リクエストスコープに保存
		model.addAttribute("order", bean);
		model.addAttribute("orderItemBeans", orderItemBeans);
		model.addAttribute("total", total);
		return "client/order/detail";
	}

	@Transactional
	@RequestMapping(path = "/client/order/cancel/{id}", method = RequestMethod.POST)
	public String cancelOrder(@PathVariable Integer id) {
		UserBean user = (UserBean) session.getAttribute("user");
		if (user == null) {
			return "redirect:/login";
		}

		Order order = orderRepository.findByIdAndUserId(id, user.getId());
		if (order == null) {
			return "redirect:/syserror";
		}

		if (!ORDER_CANCELED.equals(order.getCancelFlag())) {
			order.setCancelFlag(ORDER_CANCELED);
			order.setCancelDate(new Date(System.currentTimeMillis()));
			restoreItemStock(order);
			orderRepository.save(order);
		}
		return "redirect:/client/order/cancel/complete";
	}

	private void restoreItemStock(Order order) {
		if (order.getOrderItemsList() == null) {
			return;
		}
		for (OrderItem orderItem : order.getOrderItemsList()) {
			if (orderItem.getItem() == null || orderItem.getItem().getId() == null || orderItem.getQuantity() == null) {
				continue;
			}
			itemRepository.findById(orderItem.getItem().getId()).ifPresent(item -> {
				Integer currentStock = item.getStock() == null ? 0 : item.getStock();
				item.setStock(currentStock + orderItem.getQuantity());
				itemRepository.save(item);
			});
		}
	}

	@RequestMapping(path = "/client/order/cancel/complete", method = RequestMethod.GET)
	public String cancelComplete() {
		return "client/order/cancel_complete";
	}
}
