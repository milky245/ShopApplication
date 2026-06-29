package jp.co.sss.shop.controller.client.basket;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.bean.BasketBean;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.repository.ItemRepository;
import jp.co.sss.shop.util.Constant;

/**
 * 買い物かご機能(一般会員用)のコントローラクラスです。
 * @author シュエ　ジーハン
 */
@Controller
public class ClientBasketController {

	/**
	 * 買い物かご情報のセッション属性名
	 */
	private static final String BASKET_BEANS = "basketBeans";

	/**
	 * 在庫不足商品名リストの属性名
	 */
	private static final String ITEM_NAME_LIST_LESS_THAN = "itemNameListLessThan";

	/**
	 * 在庫切れ商品名リストの属性名
	 */
	private static final String ITEM_NAME_LIST_ZERO = "itemNameListZero";

	/**
	 * 商品情報リポジトリ
	 */
	@Autowired
	ItemRepository itemRepository;

	/**
	 * セッション
	 */
	@Autowired
	HttpSession session;

	/**
	 * 買い物かご画面を表示します。
	 *
	 * @author シュエ　ジーハン
	 * @param model Viewとの値受渡し
	 * @return "client/basket/list" 買い物かご画面
	 * @see jp.co.sss.shop.bean.BasketBean
	 */
	@RequestMapping(path = "/client/basket/list", method = { RequestMethod.GET, RequestMethod.POST })
	public String showBasket(Model model) {
		List<String> itemNameListLessThan = drainMessageList(ITEM_NAME_LIST_LESS_THAN);
		List<String> itemNameListZero = drainMessageList(ITEM_NAME_LIST_ZERO);

		List<BasketBean> basketBeans = getBasketBeans();
		if (basketBeans != null) {
			Iterator<BasketBean> iterator = basketBeans.iterator();
			while (iterator.hasNext()) {
				BasketBean basketBean = iterator.next();
				Item item = itemRepository.findByIdAndDeleteFlag(basketBean.getId(), Constant.NOT_DELETED);
				if (item == null || item.getStock() == null || item.getStock() == 0) {
					// 在庫切れまたは削除済みの商品は買い物かごから外す。
					itemNameListZero.add(basketBean.getName());
					iterator.remove();
					continue;
				}

				// 最新の商品名と在庫数を買い物かご表示へ反映する。
				basketBean.setName(item.getName());
				basketBean.setStock(item.getStock());
				if (basketBean.getOrderNum() > item.getStock()) {
					// 在庫数が減っている場合は注文可能数に補正する。
					basketBean.setOrderNum(item.getStock());
					itemNameListLessThan.add(item.getName());
				}
			}
			saveBasketBeans(basketBeans);
		}

		if (!itemNameListLessThan.isEmpty()) {
			model.addAttribute(ITEM_NAME_LIST_LESS_THAN, itemNameListLessThan);
		}
		if (!itemNameListZero.isEmpty()) {
			model.addAttribute(ITEM_NAME_LIST_ZERO, itemNameListZero);
		}
		return "client/basket/list";
	}

	/**
	 * 商品を買い物かごに追加します。
	 *
	 * @author シュエ　ジーハン
	 * @param id 商品ID
	 * @return "redirect:/client/basket/list" 買い物かご画面表示処理へリダイレクト
	 * @see jp.co.sss.shop.entity.Item
	 */
	@RequestMapping(path = "/client/basket/add", method = RequestMethod.POST)
	public String addItem(@RequestParam Integer id) {
		Item item = itemRepository.findByIdAndDeleteFlag(id, Constant.NOT_DELETED);
		if (item == null) {
			return "redirect:/syserror";
		}

		if (item.getStock() == null || item.getStock() == 0) {
			// 在庫切れの商品は買い物かごへ追加せずメッセージを保存する。
			addSessionMessage(ITEM_NAME_LIST_ZERO, item.getName());
			return "redirect:/client/basket/list";
		}

		List<BasketBean> basketBeans = getBasketBeans();
		if (basketBeans == null) {
			basketBeans = new ArrayList<BasketBean>();
		}

		for (BasketBean basketBean : basketBeans) {
			if (basketBean.getId().equals(item.getId())) {
				if (basketBean.getOrderNum() >= item.getStock()) {
					// 買い物かご内の数が在庫数に達している場合は数を増やさない。
					addSessionMessage(ITEM_NAME_LIST_LESS_THAN, item.getName());
				} else {
					// 同一商品がある場合は注文数を1増やす。
					basketBean.setOrderNum(basketBean.getOrderNum() + 1);
					basketBean.setStock(item.getStock());
				}
				saveBasketBeans(basketBeans);
				return "redirect:/client/basket/list";
			}
		}

		// 初めて追加する商品はBasketBeanとしてセッションへ保存する。
		basketBeans.add(new BasketBean(item.getId(), item.getName(), item.getStock()));
		saveBasketBeans(basketBeans);
		return "redirect:/client/basket/list";
	}

	/**
	 * 買い物かご内の商品を削除します。
	 *
	 * @author シュエ　ジーハン
	 * @param id 商品ID
	 * @return "redirect:/client/basket/list" 買い物かご画面表示処理へリダイレクト
	 * @see jp.co.sss.shop.bean.BasketBean
	 */
	@RequestMapping(path = "/client/basket/delete", method = RequestMethod.POST)
	public String deleteItem(@RequestParam Integer id) {
		List<BasketBean> basketBeans = getBasketBeans();
		if (basketBeans != null) {
			// 指定された商品IDの買い物かご情報を削除する。
			Iterator<BasketBean> iterator = basketBeans.iterator();
			while (iterator.hasNext()) {
				BasketBean basketBean = iterator.next();
				if (basketBean.getId().equals(id)) {
					if (basketBean.getOrderNum() != null && basketBean.getOrderNum() > 1) {
						basketBean.setOrderNum(basketBean.getOrderNum() - 1);
					} else {
						iterator.remove();
					}
					break;
				}
			}
			saveBasketBeans(basketBeans);
		}
		return "redirect:/client/basket/list";
	}

	/**
	 * 買い物かご内の商品をすべて削除します。
	 *
	 * @author シュエ　ジーハン
	 * @return "redirect:/client/basket/list" 買い物かご画面表示処理へリダイレクト
	 * @see jakarta.servlet.http.HttpSession#removeAttribute(String)
	 */
	@RequestMapping(path = "/client/basket/allDelete", method = RequestMethod.POST)
	public String deleteAllItems() {
		// 買い物かご情報をセッションから削除する。
		session.removeAttribute(BASKET_BEANS);
		return "redirect:/client/basket/list";
	}

	/**
	 * セッションから買い物かご情報を取得します。
	 *
	 * @author シュエ　ジーハン
	 * @return 買い物かご情報
	 * @see jakarta.servlet.http.HttpSession#getAttribute(String)
	 */
	@SuppressWarnings("unchecked")
	private List<BasketBean> getBasketBeans() {
		return (List<BasketBean>) session.getAttribute(BASKET_BEANS);
	}

	/**
	 * 買い物かご情報をセッションへ保存します。
	 *
	 * @author シュエ　ジーハン
	 * @param basketBeans 買い物かご情報
	 * @see jakarta.servlet.http.HttpSession#setAttribute(String, Object)
	 * @see jakarta.servlet.http.HttpSession#removeAttribute(String)
	 */
	private void saveBasketBeans(List<BasketBean> basketBeans) {
		if (basketBeans == null || basketBeans.isEmpty()) {
			session.removeAttribute(BASKET_BEANS);
		} else {
			session.setAttribute(BASKET_BEANS, basketBeans);
		}
	}

	/**
	 * セッションに一時メッセージ用の商品名を追加します。
	 *
	 * @author シュエ　ジーハン
	 * @param attributeName 属性名
	 * @param itemName 商品名
	 * @see jakarta.servlet.http.HttpSession#setAttribute(String, Object)
	 */
	private void addSessionMessage(String attributeName, String itemName) {
		List<String> itemNameList = drainMessageList(attributeName);
		itemNameList.add(itemName);
		session.setAttribute(attributeName, itemNameList);
	}

	/**
	 * セッションから一時メッセージリストを取得して削除します。
	 *
	 * @author シュエ　ジーハン
	 * @param attributeName 属性名
	 * @return 商品名リスト
	 * @see jakarta.servlet.http.HttpSession#getAttribute(String)
	 * @see jakarta.servlet.http.HttpSession#removeAttribute(String)
	 */
	@SuppressWarnings("unchecked")
	private List<String> drainMessageList(String attributeName) {
		List<String> itemNameList = (List<String>) session.getAttribute(attributeName);
		session.removeAttribute(attributeName);
		if (itemNameList == null) {
			return new ArrayList<String>();
		}
		return itemNameList;
	}
}
