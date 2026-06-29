package jp.co.sss.shop.controller.client.item;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jp.co.sss.shop.bean.ItemBean;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.util.Constant;

@SpringBootTest
@Transactional
class ClientItemShowControllerIntegrationTest {

	@Autowired
	private ClientItemShowController controller;

	@Autowired
	private EntityManager entityManager;

	@Test
	void showItemDisplaysRelatedItemsBeforeRecentlyViewedItems() throws Exception {
		insertTestData();
		entityManager.clear();

		UserBean loginUser = new UserBean();
		loginUser.setId(-72);
		MockHttpSession session = new MockHttpSession();
		session.setAttribute("user", loginUser);
		Model model = new ConcurrentModel();

		String view = controller.showItem(-620, session, model);

		assertEquals("client/item/detail", view);
		assertEquals(List.of(-621), itemIds(model.getAttribute("relatedItems")));
		assertEquals(List.of(-621), itemIds(model.getAttribute("recentlyViewedItems")));

		String template = new ClassPathResource("templates/client/item/detail.html")
				.getContentAsString(UTF_8);
		assertTrue(template.indexOf("related_items") < template.indexOf("recentlyViewedItems"));
	}

	private List<Integer> itemIds(Object modelAttribute) {
		@SuppressWarnings("unchecked")
		List<ItemBean> items = (List<ItemBean>) modelAttribute;
		return items.stream().map(ItemBean::getId).toList();
	}

	private void insertTestData() {
		entityManager.createNativeQuery("""
				INSERT INTO categories (id, name, description, delete_flag, insert_date)
				VALUES (-54, '統合テストカテゴリ', '統合テストカテゴリ', 0, SYSDATE)
				""").executeUpdate();

		insertItem(-620, "表示中商品");
		insertItem(-621, "関連・閲覧履歴商品");

		entityManager.createNativeQuery("""
				INSERT INTO users
					(id, email, password, name, postal_code, address, phone_number,
					 authority, delete_flag, insert_date)
				VALUES
					(-72, 'combined-feature-test@example.com', 'password1', '統合テスト会員',
					 '1234567', 'テスト住所', '09012345678', :authority, 0, SYSDATE)
				""")
				.setParameter("authority", Constant.AUTH_CLIENT)
				.executeUpdate();

		insertViewHistory(-91, -620, "2025-01-01 10:00:00");
		insertViewHistory(-92, -621, "2025-01-02 10:00:00");
	}

	private void insertItem(int id, String name) {
		entityManager.createNativeQuery("""
				INSERT INTO items
					(id, name, price, description, stock, image, delete_flag, insert_date, category_id)
				VALUES
					(:id, :name, 100, :name, 10, NULL, 0, SYSDATE, -54)
				""")
				.setParameter("id", id)
				.setParameter("name", name)
				.executeUpdate();
	}

	private void insertViewHistory(int id, int itemId, String viewDate) {
		entityManager.createNativeQuery("""
				INSERT INTO view_histories (id, user_id, item_id, view_date)
				VALUES (:id, -72, :itemId, TO_TIMESTAMP(:viewDate, 'YYYY-MM-DD HH24:MI:SS'))
				""")
				.setParameter("id", id)
				.setParameter("itemId", itemId)
				.setParameter("viewDate", viewDate)
				.executeUpdate();
	}
}
