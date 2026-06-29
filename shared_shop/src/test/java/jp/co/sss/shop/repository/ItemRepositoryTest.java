package jp.co.sss.shop.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.util.Constant;

@SpringBootTest
@Transactional
class ItemRepositoryTest {

	private static final int TARGET_CATEGORY_ID = -51;
	private static final int OTHER_CATEGORY_ID = -52;
	private static final int CURRENT_ITEM_ID = -601;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void findRelatedItemsFiltersSortsAndLimitsCandidates() {
		insertCategory(TARGET_CATEGORY_ID, "関連商品テストカテゴリ", Constant.NOT_DELETED);
		insertCategory(OTHER_CATEGORY_ID, "別カテゴリ", Constant.NOT_DELETED);

		insertItem(CURRENT_ITEM_ID, "表示中商品", 10, TARGET_CATEGORY_ID, Constant.NOT_DELETED, "2025-01-01");
		insertItem(-602, "売上10個", 10, TARGET_CATEGORY_ID, Constant.NOT_DELETED, "2025-01-01");
		insertItem(-603, "売上5個", 10, TARGET_CATEGORY_ID, Constant.NOT_DELETED, "2025-01-01");
		insertItem(-604, "注文なし新着", 10, TARGET_CATEGORY_ID, Constant.NOT_DELETED, "2025-01-02");
		insertItem(-606, "注文なし旧1", 10, TARGET_CATEGORY_ID, Constant.NOT_DELETED, "2025-01-01");
		insertItem(-605, "注文なし旧2", 10, TARGET_CATEGORY_ID, Constant.NOT_DELETED, "2025-01-01");
		insertItem(-607, "5件目候補", 10, TARGET_CATEGORY_ID, Constant.NOT_DELETED, "2024-12-31");
		insertItem(-608, "在庫切れ", 0, TARGET_CATEGORY_ID, Constant.NOT_DELETED, "2025-01-03");
		insertItem(-609, "削除済み", 10, TARGET_CATEGORY_ID, Constant.DELETED, "2025-01-03");
		insertItem(-610, "別カテゴリ商品", 10, OTHER_CATEGORY_ID, Constant.NOT_DELETED, "2025-01-03");

		insertOrderData();
		entityManager.clear();

		List<Item> actual = itemRepository.findRelatedItems(
				TARGET_CATEGORY_ID,
				CURRENT_ITEM_ID,
				Constant.NOT_DELETED,
				PageRequest.of(0, 4));

		assertEquals(
				List.of(-602, -603, -604, -605),
				actual.stream().map(Item::getId).toList());
	}

	@Test
	void findRelatedItemsExcludesItemsInDeletedCategory() {
		int deletedCategoryId = -53;
		insertCategory(deletedCategoryId, "削除済みカテゴリ", Constant.DELETED);
		insertItem(-611, "表示中商品", 10, deletedCategoryId, Constant.NOT_DELETED, "2025-01-01");
		insertItem(-612, "同一カテゴリ商品", 10, deletedCategoryId, Constant.NOT_DELETED, "2025-01-01");
		entityManager.clear();

		List<Item> actual = itemRepository.findRelatedItems(
				deletedCategoryId,
				-611,
				Constant.NOT_DELETED,
				PageRequest.of(0, 4));

		assertTrue(actual.isEmpty());
	}

	private void insertCategory(int id, String name, int deleteFlag) {
		entityManager.createNativeQuery("""
				INSERT INTO categories (id, name, description, delete_flag, insert_date)
				VALUES (:id, :name, :description, :deleteFlag, SYSDATE)
				""")
				.setParameter("id", id)
				.setParameter("name", name)
				.setParameter("description", name)
				.setParameter("deleteFlag", deleteFlag)
				.executeUpdate();
	}

	private void insertItem(
			int id,
			String name,
			int stock,
			int categoryId,
			int deleteFlag,
			String insertDate) {
		entityManager.createNativeQuery("""
				INSERT INTO items
					(id, name, price, description, stock, image, delete_flag, insert_date, category_id)
				VALUES
					(:id, :name, 100, :description, :stock, NULL, :deleteFlag,
					 TO_DATE(:insertDate, 'YYYY-MM-DD'), :categoryId)
				""")
				.setParameter("id", id)
				.setParameter("name", name)
				.setParameter("description", name)
				.setParameter("stock", stock)
				.setParameter("deleteFlag", deleteFlag)
				.setParameter("insertDate", insertDate)
				.setParameter("categoryId", categoryId)
				.executeUpdate();
	}

	private void insertOrderData() {
		entityManager.createNativeQuery("""
				INSERT INTO users
					(id, email, password, name, postal_code, address, phone_number,
					 authority, delete_flag, insert_date)
				VALUES
					(-701, 'recommend-test@example.com', 'password1', '関連商品テスト会員',
					 '1234567', 'テスト住所', '09012345678', :authority, :deleteFlag, SYSDATE)
				""")
				.setParameter("authority", Constant.AUTH_CLIENT)
				.setParameter("deleteFlag", Constant.NOT_DELETED)
				.executeUpdate();

		entityManager.createNativeQuery("""
				INSERT INTO orders
					(id, postal_code, address, name, phone_number, pay_method, insert_date, user_id)
				VALUES
					(-801, '1234567', 'テスト住所', '関連商品テスト会員',
					 '09012345678', 1, SYSDATE, -701)
				""")
				.executeUpdate();

		insertOrderItem(-901, -602, 10);
		insertOrderItem(-902, -603, 5);
	}

	private void insertOrderItem(int id, int itemId, int quantity) {
		entityManager.createNativeQuery("""
				INSERT INTO order_items (id, quantity, order_id, item_id, price)
				VALUES (:id, :quantity, -801, :itemId, 100)
				""")
				.setParameter("id", id)
				.setParameter("quantity", quantity)
				.setParameter("itemId", itemId)
				.executeUpdate();
	}
}
