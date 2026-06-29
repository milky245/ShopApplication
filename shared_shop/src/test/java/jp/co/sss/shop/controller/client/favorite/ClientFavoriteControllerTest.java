package jp.co.sss.shop.controller.client.favorite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import jp.co.sss.shop.bean.ItemBean;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.Favorite;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.repository.FavoriteRepository;
import jp.co.sss.shop.repository.ItemRepository;
import jp.co.sss.shop.service.BeanTools;
import jp.co.sss.shop.util.Constant;

class ClientFavoriteControllerTest {

	@InjectMocks
	private ClientFavoriteController controller;

	@Mock
	private FavoriteRepository favoriteRepository;

	@Mock
	private BeanTools beanTools;

	private MockHttpSession session;
	private Model model;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		session = new MockHttpSession();
		model = new ConcurrentModel();
	}

	@Test
	void listReturnsFavoriteListWhenLoggedIn() {
		UserBean userBean = new UserBean();
		userBean.setId(1);
		session.setAttribute("user", userBean);

		List<Favorite> favoriteList = new ArrayList<>();
		Favorite favorite = new Favorite();
		Item item = new Item();
		item.setDeleteFlag(Constant.NOT_DELETED);
		favorite.setItem(item);
		favoriteList.add(favorite);

		when(favoriteRepository.findByUserIdOrderByInsertDateDesc(1)).thenReturn(favoriteList);
		when(beanTools.copyEntityToItemBean(any(Item.class))).thenReturn(new ItemBean());

		String view = controller.list(session, model);

		assertEquals("client/favorite/list", view);
		@SuppressWarnings("unchecked")
		List<ItemBean> items = (List<ItemBean>) model.getAttribute("items");
		assertEquals(1, items.size());
	}

	@Test
	void listRedirectsToLoginWhenNotLoggedIn() {
		String view = controller.list(session, model);
		assertEquals("redirect:/login", view);
	}

	@Test
	void registAddsFavoriteWhenNotExists() {
		UserBean userBean = new UserBean();
		userBean.setId(1);
		session.setAttribute("user", userBean);

		when(favoriteRepository.existsByUserIdAndItemId(1, 100)).thenReturn(false);

		String view = controller.regist(100, session);

		assertEquals("redirect:/client/item/detail/100", view);
		verify(favoriteRepository).save(any(Favorite.class));
	}

	@Test
	void registDoesNotAddFavoriteWhenAlreadyExists() {
		UserBean userBean = new UserBean();
		userBean.setId(1);
		session.setAttribute("user", userBean);

		when(favoriteRepository.existsByUserIdAndItemId(1, 100)).thenReturn(true);

		String view = controller.regist(100, session);

		assertEquals("redirect:/client/item/detail/100", view);
		verify(favoriteRepository, never()).save(any(Favorite.class));
	}

	@Test
	void deleteRemovesFavorite() {
		UserBean userBean = new UserBean();
		userBean.setId(1);
		session.setAttribute("user", userBean);
		MockHttpServletRequest request = new MockHttpServletRequest();

		String view = controller.delete(100, session, request);

		assertEquals("redirect:/client/item/detail/100", view);
		verify(favoriteRepository).deleteByUserIdAndItemId(1, 100);
	}

	@Test
	void deleteRedirectsToListWhenRefererIsList() {
		UserBean userBean = new UserBean();
		userBean.setId(1);
		session.setAttribute("user", userBean);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Referer", "/client/favorite/list");

		String view = controller.delete(100, session, request);

		assertEquals("redirect:/client/favorite/list", view);
		verify(favoriteRepository).deleteByUserIdAndItemId(1, 100);
	}
}
