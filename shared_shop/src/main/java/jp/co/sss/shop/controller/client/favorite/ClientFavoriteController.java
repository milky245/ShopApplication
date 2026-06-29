package jp.co.sss.shop.controller.client.favorite;

import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import jp.co.sss.shop.bean.ItemBean;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.Favorite;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.repository.FavoriteRepository;
import jp.co.sss.shop.repository.ItemRepository;
import jp.co.sss.shop.service.BeanTools;
import jp.co.sss.shop.util.Constant;
import org.springframework.transaction.annotation.Transactional;

/**
 * お気に入り機能のコントローラクラス
 */
@Controller
public class ClientFavoriteController {

	/**
	 * お気に入り情報
	 */
	@Autowired
	FavoriteRepository favoriteRepository;

	/**
	 * Entity、Form、Bean間のデータコピーサービス
	 */
	@Autowired
	BeanTools beanTools;

	/**
	 * お気に入り一覧表示
	 * @param session セッション
	 * @param model Model
	 * @return お気に入り一覧画面
	 */
	@RequestMapping(path = "/client/favorite/list", method = RequestMethod.GET)
	public String list(HttpSession session, Model model) {
		UserBean userBean = (UserBean) session.getAttribute("user");
		if (userBean == null) {
			return "redirect:/login";
		}

		List<Favorite> favoriteList = favoriteRepository.findByUserIdOrderByInsertDateDesc(userBean.getId());
		List<ItemBean> itemBeanList = new ArrayList<>();
		for (Favorite favorite : favoriteList) {
			Item item = favorite.getItem();
			// 削除フラグが立っていない商品のみ表示
			if (item.getDeleteFlag() == Constant.NOT_DELETED) {
				itemBeanList.add(beanTools.copyEntityToItemBean(item));
			}
		}

		model.addAttribute("items", itemBeanList);
		return "client/favorite/list";
	}

	/**
	 * お気に入り登録
	 * @param itemId 商品ID
	 * @param session セッション
	 * @return 商品詳細画面へリダイレクト
	 */
	@RequestMapping(path = "/client/favorite/regist/{itemId}", method = RequestMethod.POST)
	public String regist(@PathVariable Integer itemId, HttpSession session) {
		UserBean userBean = (UserBean) session.getAttribute("user");
		if (userBean == null) {
			return "redirect:/login";
		}

		// 重複登録チェック
		if (!favoriteRepository.existsByUserIdAndItemId(userBean.getId(), itemId)) {
			Favorite favorite = new Favorite();
			User user = new User();
			user.setId(userBean.getId());
			Item item = new Item();
			item.setId(itemId);
			favorite.setUser(user);
			favorite.setItem(item);
			favoriteRepository.save(favorite);
		}

		return "redirect:/client/item/detail/" + itemId;
	}

	/**
	 * お気に入り解除
	 * @param itemId 商品ID
	 * @param session セッション
	 * @return 遷移元画面へリダイレクト
	 */
	@Transactional
	@RequestMapping(path = "/client/favorite/delete/{itemId}", method = RequestMethod.POST)
	public String delete(@PathVariable Integer itemId, HttpSession session, jakarta.servlet.http.HttpServletRequest request) {
		UserBean userBean = (UserBean) session.getAttribute("user");
		if (userBean == null) {
			return "redirect:/login";
		}

		favoriteRepository.deleteByUserIdAndItemId(userBean.getId(), itemId);

		String referer = request.getHeader("Referer");
		if (referer != null && referer.contains("/client/favorite/list")) {
			return "redirect:/client/favorite/list";
		}
		return "redirect:/client/item/detail/" + itemId;
	}
}
