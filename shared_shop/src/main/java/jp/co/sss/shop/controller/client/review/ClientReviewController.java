package jp.co.sss.shop.controller.client.review;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.OrderItem;
import jp.co.sss.shop.entity.Review;
import jp.co.sss.shop.form.ReviewForm;
import jp.co.sss.shop.repository.OrderItemRepository;
import jp.co.sss.shop.repository.ReviewRepository;
import jp.co.sss.shop.util.Constant;

/**
 * レビュー管理(一般会員用)のコントローラクラス
 */
@Controller
public class ClientReviewController {

	/**
	 * レビュー情報リポジトリ
	 */
	@Autowired
	ReviewRepository reviewRepository;

	/**
	 * 注文商品情報リポジトリ
	 */
	@Autowired
	OrderItemRepository orderItemRepository;

	/**
	 * セッション
	 */
	@Autowired
	HttpSession session;

	/**
	 * レビュー登録画面表示
	 *
	 * @param orderItemId 注文商品ID
	 * @param model       Viewとの値受渡し
	 * @return "client/review/regist_input" レビュー登録画面
	 */
	@RequestMapping(path = "/client/review/regist/input/{orderItemId}", method = RequestMethod.GET)
	public String registInput(@PathVariable Integer orderItemId, Model model) {

		// ログインユーザー情報の取得
		UserBean user = (UserBean) session.getAttribute("user");

		// 注文商品情報の取得
		Optional<OrderItem> orderItemOpt = orderItemRepository.findById(orderItemId);
		if (orderItemOpt.isEmpty()) {
			return "redirect:/syserror";
		}
		OrderItem orderItem = orderItemOpt.get();

		// 購入者本人の注文であることを確認
		if (!orderItem.getOrder().getUser().getId().equals(user.getId())) {
			return "redirect:/syserror";
		}

		// 既にレビュー済みでないか確認
		Review existingReview = reviewRepository.findByOrderItemId(orderItemId);
		if (existingReview != null) {
			// 二重投稿防止（本来はエラーメッセージを出すべきだが、仕様にないので安全に詳細画面へ戻す等）
			return "redirect:/client/item/detail/" + orderItem.getItem().getId();
		}

		// フォームの初期設定
		ReviewForm form = new ReviewForm();
		form.setOrderItemId(orderItemId);
		form.setItemId(orderItem.getItem().getId());
		model.addAttribute("reviewForm", form);
		model.addAttribute("orderItem", orderItem);

		return "client/review/regist_input";
	}

	/**
	 * レビュー登録処理
	 *
	 * @param form          レビューフォーム
	 * @param result        バリデーション結果
	 * @param model         Viewとの値受渡し
	 * @return "redirect:/client/item/detail/{itemId}" 商品詳細画面へ
	 */
	@RequestMapping(path = "/client/review/regist/complete", method = RequestMethod.POST)
	public String registComplete(@Valid @ModelAttribute ReviewForm form, BindingResult result, Model model) {

		if (result.hasErrors()) {
			// 注文商品情報を再取得してモデルにセット（エラー表示用）
			OrderItem orderItem = orderItemRepository.findById(form.getOrderItemId()).orElse(null);
			model.addAttribute("orderItem", orderItem);
			return "client/review/regist_input";
		}

		// ログインユーザー情報の取得
		UserBean user = (UserBean) session.getAttribute("user");

		// 注文商品情報の取得と権限チェック
		Optional<OrderItem> orderItemOpt = orderItemRepository.findById(form.getOrderItemId());
		if (orderItemOpt.isEmpty()) {
			return "redirect:/syserror";
		}
		OrderItem orderItem = orderItemOpt.get();

		// 購入者本人の注文であることを確認 (セキュリティ対策)
		if (!orderItem.getOrder().getUser().getId().equals(user.getId())) {
			return "redirect:/syserror";
		}

		// 既にレビュー済みでないか再確認
		Review existingReview = reviewRepository.findByOrderItemId(form.getOrderItemId());
		if (existingReview != null) {
			return "redirect:/client/item/detail/" + form.getItemId();
		}

		// エンティティに詰め替えて保存
		Review review = new Review();
		review.setOrderItem(orderItem);
		review.setItem(orderItem.getItem());
		review.setUser(orderItem.getOrder().getUser());
		review.setRating(form.getRating());
		review.setReviewComment(form.getReviewComment());

		reviewRepository.save(review);

		return "redirect:/client/item/detail/" + form.getItemId();
	}

	/**
	 * レビュー削除処理
	 *
	 * @param id レビューID
	 * @return 商品詳細画面へリダイレクト
	 */
	@RequestMapping(path = "/client/review/delete/{id}", method = RequestMethod.POST)
	public String delete(@PathVariable Integer id) {

		// ログインユーザー情報の取得
		UserBean user = (UserBean) session.getAttribute("user");

		// 削除対象のレビューを取得
		Optional<Review> reviewOpt = reviewRepository.findById(id);
		if (reviewOpt.isEmpty()) {
			return "redirect:/syserror";
		}
		Review review = reviewOpt.get();

		// 権限チェック (自分のレビューか、ADMIN権限か)
		boolean isOwner = review.getUser().getId().equals(user.getId());
		boolean isAdmin = user.getAuthority() == Constant.AUTH_ADMIN;

		if (isOwner || isAdmin) {
			reviewRepository.delete(review);
		} else {
			return "redirect:/syserror";
		}

		return "redirect:/client/item/detail/" + review.getItem().getId();
	}
}
