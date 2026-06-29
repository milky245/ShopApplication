package jp.co.sss.shop.controller.client.coupon;

import java.security.SecureRandom;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.bean.UserCouponBean;
import jp.co.sss.shop.entity.CouponGachaHistory;
import jp.co.sss.shop.entity.CouponType;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.entity.UserCoupon;
import jp.co.sss.shop.repository.CouponGachaHistoryRepository;
import jp.co.sss.shop.repository.CouponTypeRepository;
import jp.co.sss.shop.repository.UserCouponRepository;
import jp.co.sss.shop.repository.UserRepository;
import jp.co.sss.shop.util.Constant;

/**
 * クーポン一覧およびクーポンガチャ機能のコントローラクラスです。
 */
@Controller
public class ClientCouponController {

	static final int MAX_COUPON_COUNT = 20;
	static final int ACTIVE_COUPON_TYPE = 1;
	private static final int BUSINESS_DAY_START_HOUR = 7;
	private static final ZoneId JAPAN_ZONE = ZoneId.of("Asia/Tokyo");
	private static final DateTimeFormatter DISPLAY_DATE_TIME =
			DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

	@Autowired
	CouponTypeRepository couponTypeRepository;

	@Autowired
	UserCouponRepository userCouponRepository;

	@Autowired
	CouponGachaHistoryRepository couponGachaHistoryRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	HttpSession session;

	private final SecureRandom secureRandom = new SecureRandom();

	/**
	 * ログイン会員が保有する有効なクーポンを表示します。
	 */
	@Transactional
	@RequestMapping(path = "/client/coupon/list", method = RequestMethod.GET)
	public String list(Model model) {
		UserBean loginUser = getLoginUser();
		if (loginUser == null) {
			return "redirect:/login";
		}

		Timestamp now = currentTimestamp();
		userCouponRepository.deleteExpired(now);
		List<UserCoupon> coupons = userCouponRepository.findAvailableByUserId(loginUser.getId(), now);
		model.addAttribute("couponBeans", toBeans(coupons, null));
		model.addAttribute("couponCount", coupons.size());
		model.addAttribute("couponLimit", MAX_COUPON_COUNT);
		return "client/coupon/list";
	}

	/**
	 * クーポンガチャ画面を表示します。
	 */
	@Transactional
	@RequestMapping(path = "/client/coupon/gacha", method = RequestMethod.GET)
	public String gacha(Model model) {
		UserBean loginUser = getLoginUser();
		if (loginUser == null) {
			return "redirect:/login";
		}

		Timestamp now = currentTimestamp();
		userCouponRepository.deleteExpired(now);
		long couponCount = userCouponRepository.countByUserIdAndExpiresAtAfter(loginUser.getId(), now);
		boolean drawnToday = couponGachaHistoryRepository.existsByUserIdAndBusinessDate(
				loginUser.getId(), Date.valueOf(currentBusinessDate()));

		model.addAttribute("couponLineup", toTypeBeans(
				couponTypeRepository.findByActiveFlagOrderByDiscountRateAsc(ACTIVE_COUPON_TYPE)));
		model.addAttribute("couponCount", couponCount);
		model.addAttribute("couponLimit", MAX_COUPON_COUNT);
		model.addAttribute("canDraw", !drawnToday && couponCount < MAX_COUPON_COUNT);
		model.addAttribute("drawnToday", drawnToday);
		model.addAttribute("nextDrawAt", nextBusinessDayStart().format(DISPLAY_DATE_TIME));
		moveSessionMessageToModel(model);
		return "client/coupon/gacha";
	}

	/**
	 * 1日1回のクーポン抽選を行います。
	 */
	@Transactional
	@RequestMapping(path = "/client/coupon/gacha/draw", method = RequestMethod.POST)
	public String draw() {
		UserBean loginUser = getLoginUser();
		if (loginUser == null) {
			return "redirect:/login";
		}

		User user = userRepository.findByIdAndDeleteFlagForUpdate(
				loginUser.getId(), Constant.NOT_DELETED);
		if (user == null) {
			return "redirect:/syserror";
		}

		Timestamp now = currentTimestamp();
		userCouponRepository.deleteExpired(now);
		long couponCount = userCouponRepository.countByUserIdAndExpiresAtAfter(user.getId(), now);
		if (couponCount >= MAX_COUPON_COUNT) {
			setMessage("保有上限の20枚に達しているため、新しいクーポンを獲得できません。", "limit");
			return "redirect:/client/coupon/gacha";
		}

		Date businessDate = Date.valueOf(currentBusinessDate());
		if (couponGachaHistoryRepository.existsByUserIdAndBusinessDate(user.getId(), businessDate)) {
			setMessage("本日のクーポンガチャは実行済みです。次回は午前7時以降に挑戦できます。", "info");
			return "redirect:/client/coupon/gacha";
		}

		CouponType wonCouponType = drawCouponType();
		CouponGachaHistory history = new CouponGachaHistory();
		history.setUser(user);
		history.setBusinessDate(businessDate);
		history.setResultCouponType(wonCouponType);
		couponGachaHistoryRepository.save(history);

		if (wonCouponType == null) {
			setMessage("今回ははずれです。次回のチャレンジをお楽しみに！", "miss");
			return "redirect:/client/coupon/gacha";
		}

		UserCoupon userCoupon = new UserCoupon();
		userCoupon.setUser(user);
		userCoupon.setCouponType(wonCouponType);
		userCoupon.setExpiresAt(Timestamp.valueOf(
				now.toLocalDateTime().plusDays(wonCouponType.getValidityDays())));
		userCouponRepository.save(userCoupon);
		setMessage(wonCouponType.getName() + "が当たりました！", "win");
		return "redirect:/client/coupon/gacha";
	}

	/**
	 * ガチャ確率に従って当選クーポン種別を返します。nullははずれです。
	 */
	CouponType drawCouponType() {
		int result = secureRandom.nextInt(100);
		if (result < 40) {
			return null;
		}
		if (result < 75) {
			return couponTypeRepository.findByDiscountRateAndActiveFlag(5, ACTIVE_COUPON_TYPE);
		}
		if (result < 95) {
			return couponTypeRepository.findByDiscountRateAndActiveFlag(10, ACTIVE_COUPON_TYPE);
		}
		return couponTypeRepository.findByDiscountRateAndActiveFlag(15, ACTIVE_COUPON_TYPE);
	}

	private List<UserCouponBean> toBeans(List<UserCoupon> coupons, Integer orderTotal) {
		List<UserCouponBean> beans = new ArrayList<UserCouponBean>();
		for (UserCoupon coupon : coupons) {
			CouponType type = coupon.getCouponType();
			UserCouponBean bean = new UserCouponBean();
			bean.setId(coupon.getId());
			bean.setName(type.getName());
			bean.setDiscountRate(type.getDiscountRate());
			bean.setMinimumOrderAmount(type.getMinimumOrderAmount());
			bean.setAcquiredAt(format(coupon.getAcquiredAt()));
			bean.setExpiresAt(format(coupon.getExpiresAt()));
			boolean available = orderTotal == null || orderTotal >= type.getMinimumOrderAmount();
			bean.setAvailable(available);
			if (!available) {
				bean.setUnavailableReason("ご注文金額が最低利用金額に達していません。");
			}
			beans.add(bean);
		}
		return beans;
	}

	private List<UserCouponBean> toTypeBeans(List<CouponType> couponTypes) {
		List<UserCouponBean> beans = new ArrayList<UserCouponBean>();
		for (CouponType type : couponTypes) {
			UserCouponBean bean = new UserCouponBean();
			bean.setId(type.getId());
			bean.setName(type.getName());
			bean.setDiscountRate(type.getDiscountRate());
			bean.setMinimumOrderAmount(type.getMinimumOrderAmount());
			beans.add(bean);
		}
		return beans;
	}

	private String format(Timestamp timestamp) {
		return timestamp == null ? "" : timestamp.toLocalDateTime().format(DISPLAY_DATE_TIME);
	}

	private UserBean getLoginUser() {
		return (UserBean) session.getAttribute("user");
	}

	private Timestamp currentTimestamp() {
		return Timestamp.valueOf(LocalDateTime.now(JAPAN_ZONE));
	}

	private LocalDate currentBusinessDate() {
		return LocalDateTime.now(JAPAN_ZONE).minusHours(BUSINESS_DAY_START_HOUR).toLocalDate();
	}

	private LocalDateTime nextBusinessDayStart() {
		LocalDateTime now = LocalDateTime.now(JAPAN_ZONE);
		LocalDateTime todayStart = now.toLocalDate().atTime(BUSINESS_DAY_START_HOUR, 0);
		return now.isBefore(todayStart) ? todayStart : todayStart.plusDays(1);
	}

	private void setMessage(String message, String type) {
		session.setAttribute("couponMessage", message);
		session.setAttribute("couponMessageType", type);
	}

	private void moveSessionMessageToModel(Model model) {
		Object message = session.getAttribute("couponMessage");
		Object type = session.getAttribute("couponMessageType");
		if (message != null) {
			model.addAttribute("couponMessage", message);
			model.addAttribute("couponMessageType", type);
			session.removeAttribute("couponMessage");
			session.removeAttribute("couponMessageType");
		}
	}
}
