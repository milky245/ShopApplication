package jp.co.sss.shop.validator;

import java.sql.Timestamp;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jp.co.sss.shop.annotation.LoginCheck;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.repository.UserRepository;
import jp.co.sss.shop.util.Constant;

/**
 * ログインチェックの独自検証クラス
 *
 * @author System Shared
 */
public class LoginValidator implements ConstraintValidator<LoginCheck, Object> {
	private String email;
	private String password;

	@Autowired
	UserRepository userRepository;

	@Autowired
	HttpSession session;

	@Override
	public void initialize(LoginCheck annotation) {
		this.email = annotation.fieldEmail();
		this.password = annotation.fieldPassword();
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		BeanWrapper beanWrapper = new BeanWrapperImpl(value);
		String emailProp = (String) beanWrapper.getPropertyValue(this.email);
		String passwordProp = (String) beanWrapper.getPropertyValue(this.password);

		User user = userRepository.findByEmailAndDeleteFlag(emailProp, Constant.NOT_DELETED);

		if (user != null) {
			Timestamp now = new Timestamp(System.currentTimeMillis());
			Timestamp lockReleaseTime = user.getLockReleaseTime();

			// ロック解除判定
			if (lockReleaseTime != null && now.after(lockReleaseTime)) {
				user.setLoginFailureCount(0);
				user.setLockReleaseTime(null);
				userRepository.save(user);
				lockReleaseTime = null;
			}

			// ロック中判定
			if (lockReleaseTime != null) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("{login.locked.message}").addConstraintViolation();
				return false;
			}

			if (passwordProp.equals(user.getPassword())) {
				// 認証成功
				UserBean userBean = new UserBean();

				userBean.setId(user.getId());
				userBean.setName(user.getName());
				userBean.setAuthority(user.getAuthority());

				// セッションスコープにログインしたユーザの情報を登録
				session.setAttribute("user", userBean);

				// 失敗回数とロック解除時刻をリセット
				user.setLoginFailureCount(0);
				user.setLockReleaseTime(null);
				userRepository.save(user);

				return true;
			} else {
				// 認証失敗
				int failureCount = (user.getLoginFailureCount() == null) ? 0 : user.getLoginFailureCount();
				failureCount++;
				user.setLoginFailureCount(failureCount);

				if (failureCount >= Constant.MAX_LOGIN_FAILURE) {
					// ロック時間を設定
					long lockTimeMillis = System.currentTimeMillis() + (Constant.LOCK_DURATION_MINUTES * 60 * 1000);
					user.setLockReleaseTime(new Timestamp(lockTimeMillis));
				}
				userRepository.save(user);

				return false;
			}
		} else {
			// ユーザが存在しない、または削除済み
			return false;
		}
	}
}
