package jp.co.sss.shop.controller.client.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.form.UserForm;
import jp.co.sss.shop.repository.UserRepository;
import jp.co.sss.shop.util.Constant;

class ClientUserDeleteControllerTest {

	private ClientUserDeleteController controller;

	@Mock
	private UserRepository userRepository;

	private MockHttpSession session;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		session = new MockHttpSession();
		controller = new ClientUserDeleteController();
		controller.userRepository = userRepository;
		controller.session = session;
	}

	@Test
	void deleteCheckInitStoresCompleteUserFormFromDatabase() {
		UserBean loginUser = new UserBean();
		loginUser.setId(10);
		loginUser.setName("login name only");
		session.setAttribute("user", loginUser);

		User user = createUser(10);
		when(userRepository.findByIdAndDeleteFlag(10, Constant.NOT_DELETED)).thenReturn(user);

		String view = controller.deleteCheckInit();

		assertEquals("redirect:/client/user/delete/check", view);

		UserForm userForm = assertInstanceOf(UserForm.class, session.getAttribute("userForm"));
		assertEquals("client@example.com", userForm.getEmail());
		assertEquals("Test User", userForm.getName());
		assertEquals("1234567", userForm.getPostalCode());
		assertEquals("Tokyo test address", userForm.getAddress());
		assertEquals("09012345678", userForm.getPhoneNumber());
	}

	@Test
	void deleteCheckDisplaysStoredUserForm() {
		UserForm userForm = new UserForm();
		userForm.setEmail("client@example.com");
		session.setAttribute("userForm", userForm);
		Model model = new ConcurrentModel();

		String view = controller.deleteCheck(model);

		assertEquals("client/user/delete_check", view);
		assertEquals(userForm, model.getAttribute("userForm"));
	}

	@Test
	void deleteCompleteMarksUserDeletedAndInvalidatesSession() {
		UserForm userForm = new UserForm();
		userForm.setId(10);
		session.setAttribute("userForm", userForm);

		User user = createUser(10);
		when(userRepository.findByIdAndDeleteFlag(10, Constant.NOT_DELETED)).thenReturn(user);

		String view = controller.deleteComplete();

		assertEquals("redirect:/client/user/delete/complete", view);
		assertEquals(Constant.DELETED, user.getDeleteFlag());
		verify(userRepository).save(user);
		assertThrows(IllegalStateException.class, () -> session.getAttribute("userForm"));
	}

	private User createUser(Integer id) {
		User user = new User();
		user.setId(id);
		user.setEmail("client@example.com");
		user.setPassword("password1");
		user.setName("Test User");
		user.setPostalCode("1234567");
		user.setAddress("Tokyo test address");
		user.setPhoneNumber("09012345678");
		user.setAuthority(Constant.AUTH_CLIENT);
		user.setDeleteFlag(Constant.NOT_DELETED);
		return user;
	}
}
