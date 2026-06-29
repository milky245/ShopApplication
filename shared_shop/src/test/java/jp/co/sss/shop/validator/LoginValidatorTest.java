package jp.co.sss.shop.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpSession;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import jp.co.sss.shop.annotation.LoginCheck;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.form.LoginForm;
import jp.co.sss.shop.repository.UserRepository;
import jp.co.sss.shop.util.Constant;

class LoginValidatorTest {

	@InjectMocks
	private LoginValidator validator;

	@Mock
	private UserRepository userRepository;

	@Mock
	private MockHttpSession session;

	@Mock
	private ConstraintValidatorContext context;

	@Mock
	private LoginCheck annotation;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		when(annotation.fieldEmail()).thenReturn("email");
		when(annotation.fieldPassword()).thenReturn("password");
		validator.initialize(annotation);
	}

	@Test
	void loginSuccess() {
		LoginForm form = new LoginForm();
		form.setEmail("test@example.com");
		form.setPassword("password");

		User user = new User();
		user.setEmail("test@example.com");
		user.setPassword("password");
		user.setLoginFailureCount(3);
		user.setLockReleaseTime(null);

		when(userRepository.findByEmailAndDeleteFlag("test@example.com", Constant.NOT_DELETED)).thenReturn(user);

		assertTrue(validator.isValid(form, context));

		assertEquals(0, user.getLoginFailureCount());
		assertNull(user.getLockReleaseTime());
		verify(userRepository).save(user);
	}

	@Test
	void loginFailureIncrementsCount() {
		LoginForm form = new LoginForm();
		form.setEmail("test@example.com");
		form.setPassword("wrongpassword");

		User user = new User();
		user.setEmail("test@example.com");
		user.setPassword("password");
		user.setLoginFailureCount(0);
		user.setLockReleaseTime(null);

		when(userRepository.findByEmailAndDeleteFlag("test@example.com", Constant.NOT_DELETED)).thenReturn(user);

		assertFalse(validator.isValid(form, context));

		assertEquals(1, user.getLoginFailureCount());
		verify(userRepository).save(user);
	}

	@Test
	void loginFailureLocksAccountAtMaxFailures() {
		LoginForm form = new LoginForm();
		form.setEmail("test@example.com");
		form.setPassword("wrongpassword");

		User user = new User();
		user.setEmail("test@example.com");
		user.setPassword("password");
		user.setLoginFailureCount(4);
		user.setLockReleaseTime(null);

		when(userRepository.findByEmailAndDeleteFlag("test@example.com", Constant.NOT_DELETED)).thenReturn(user);

		assertFalse(validator.isValid(form, context));

		assertEquals(5, user.getLoginFailureCount());
		assertTrue(user.getLockReleaseTime().after(new Timestamp(System.currentTimeMillis())));
		verify(userRepository).save(user);
	}

	@Test
	void lockedAccountFailsAndSetsMessage() {
		LoginForm form = new LoginForm();
		form.setEmail("test@example.com");
		form.setPassword("password");

		User user = new User();
		user.setEmail("test@example.com");
		user.setPassword("password");
		user.setLoginFailureCount(5);
		user.setLockReleaseTime(new Timestamp(System.currentTimeMillis() + 30 * 60 * 1000));

		when(userRepository.findByEmailAndDeleteFlag("test@example.com", Constant.NOT_DELETED)).thenReturn(user);

		ConstraintViolationBuilder builder = mock(ConstraintViolationBuilder.class);
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

		assertFalse(validator.isValid(form, context));

		verify(context).disableDefaultConstraintViolation();
		verify(context).buildConstraintViolationWithTemplate("{login.locked.message}");
	}

	@Test
	void lockedAccountAutoUnlocksAfterTimePasses() {
		LoginForm form = new LoginForm();
		form.setEmail("test@example.com");
		form.setPassword("password");

		User user = new User();
		user.setEmail("test@example.com");
		user.setPassword("password");
		user.setLoginFailureCount(5);
		user.setLockReleaseTime(new Timestamp(System.currentTimeMillis() - 60 * 1000));

		when(userRepository.findByEmailAndDeleteFlag("test@example.com", Constant.NOT_DELETED)).thenReturn(user);

		assertTrue(validator.isValid(form, context));

		assertEquals(0, user.getLoginFailureCount());
		assertNull(user.getLockReleaseTime());
		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userRepository, atLeastOnce()).save(captor.capture());
	}
}
