package jp.co.sss.shop.controller.client.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import jp.co.sss.shop.bean.ItemBean;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.Category;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.entity.ViewHistory;
import jp.co.sss.shop.repository.FavoriteRepository;
import jp.co.sss.shop.repository.ItemRepository;
import jp.co.sss.shop.repository.ReviewRepository;
import jp.co.sss.shop.repository.ViewHistoryRepository;
import jp.co.sss.shop.service.BeanTools;
import jp.co.sss.shop.util.Constant;

class ClientItemShowControllerTest {

@InjectMocks
private ClientItemShowController controller;

@Mock
private ItemRepository itemRepository;

@Mock
private ViewHistoryRepository viewHistoryRepository;

@Mock
private FavoriteRepository favoriteRepository;

@Mock
private ReviewRepository reviewRepository;

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
void showItemRecordsHistoryAndFetchesRecentItemsWhenLoggedIn() {
Item item = createItem(1, 3);
Item relatedItem = createItem(2, 3);
List<Item> relatedItems = List.of(relatedItem);
List<Item> recentItems = new ArrayList<>();
recentItems.add(relatedItem);

UserBean loginUser = new UserBean();
loginUser.setId(10);
session.setAttribute("user", loginUser);

when(itemRepository.findByIdAndDeleteFlag(1, Constant.NOT_DELETED)).thenReturn(item);
when(favoriteRepository.existsByUserIdAndItemId(anyInt(), anyInt())).thenReturn(false);
when(viewHistoryRepository.findByUserAndItem(any(User.class), eq(item))).thenReturn(null);
when(viewHistoryRepository.findItemsByUser(any(User.class), eq(item), eq(PageRequest.of(0, 4))))
.thenReturn(recentItems);
when(itemRepository.findRelatedItems(3, 1, Constant.NOT_DELETED, PageRequest.of(0, 4))).thenReturn(relatedItems);
when(beanTools.copyEntityToItemBean(item)).thenReturn(new ItemBean());
when(beanTools.copyEntityListToItemBeanList(recentItems)).thenReturn(new ArrayList<>());
when(beanTools.copyEntityListToItemBeanList(relatedItems)).thenReturn(new ArrayList<>());

String view = controller.showItem(1, session, model);

assertEquals("client/item/detail", view);
verify(viewHistoryRepository).save(any(ViewHistory.class));
assertNotNull(model.getAttribute("recentlyViewedItems"));
assertNotNull(model.getAttribute("relatedItems"));
}

@Test
void showItemDoesNotRecordHistoryWhenNotLoggedIn() {
Item item = createItem(1, 3);

when(itemRepository.findByIdAndDeleteFlag(1, Constant.NOT_DELETED)).thenReturn(item);
when(favoriteRepository.existsByUserIdAndItemId(anyInt(), anyInt())).thenReturn(false);
when(itemRepository.findRelatedItems(3, 1, Constant.NOT_DELETED, PageRequest.of(0, 4)))
.thenReturn(Collections.emptyList());
when(beanTools.copyEntityToItemBean(item)).thenReturn(new ItemBean());
when(beanTools.copyEntityListToItemBeanList(Collections.emptyList())).thenReturn(Collections.emptyList());

String view = controller.showItem(1, session, model);

assertEquals("client/item/detail", view);
assertEquals(Collections.emptyList(), model.getAttribute("recentlyViewedItems"));
assertEquals(Collections.emptyList(), model.getAttribute("relatedItems"));
verify(viewHistoryRepository, never()).save(any(ViewHistory.class));
}

@Test
void showItemDisplaysRecommendationsWhenViewHistoryFails() {
Item item = createItem(1, 3);
Item relatedItem = createItem(2, 3);
List<Item> relatedItems = List.of(relatedItem);
List<ItemBean> relatedItemBeans = List.of(new ItemBean());

UserBean loginUser = new UserBean();
loginUser.setId(10);
session.setAttribute("user", loginUser);

when(itemRepository.findByIdAndDeleteFlag(1, Constant.NOT_DELETED)).thenReturn(item);
when(favoriteRepository.existsByUserIdAndItemId(anyInt(), anyInt())).thenReturn(false);
when(itemRepository.findRelatedItems(3, 1, Constant.NOT_DELETED, PageRequest.of(0, 4)))
.thenReturn(relatedItems);
when(beanTools.copyEntityToItemBean(item)).thenReturn(new ItemBean());
when(beanTools.copyEntityListToItemBeanList(relatedItems)).thenReturn(relatedItemBeans);
when(viewHistoryRepository.findByUserAndItem(any(User.class), eq(item)))
.thenThrow(new DataRetrievalFailureException("view_histories is unavailable"));

String view = controller.showItem(1, session, model);

assertEquals("client/item/detail", view);
assertEquals(relatedItemBeans, model.getAttribute("relatedItems"));
assertEquals(Collections.emptyList(), model.getAttribute("recentlyViewedItems"));
}

@Test
void showItemDisplaysNormallyWhenNoRelatedItemExists() {
Item item = createItem(10, 3);
ItemBean itemBean = new ItemBean();

when(itemRepository.findByIdAndDeleteFlag(10, Constant.NOT_DELETED)).thenReturn(item);
when(favoriteRepository.existsByUserIdAndItemId(anyInt(), anyInt())).thenReturn(false);
when(itemRepository.findRelatedItems(3, 10, Constant.NOT_DELETED, PageRequest.of(0, 4)))
.thenReturn(Collections.emptyList());
when(beanTools.copyEntityToItemBean(item)).thenReturn(itemBean);
when(beanTools.copyEntityListToItemBeanList(Collections.emptyList())).thenReturn(Collections.emptyList());

String view = controller.showItem(10, session, model);

assertEquals("client/item/detail", view);
assertEquals(Collections.emptyList(), model.getAttribute("relatedItems"));
}

@Test
void showItemRedirectsToSystemErrorWhenCurrentItemDoesNotExist() {
when(itemRepository.findByIdAndDeleteFlag(99, Constant.NOT_DELETED)).thenReturn(null);

String view = controller.showItem(99, session, model);

assertEquals("redirect:/syserror", view);
verify(itemRepository, never()).findRelatedItems(anyInt(), anyInt(), anyInt(), any(Pageable.class));
verify(viewHistoryRepository, never()).save(any(ViewHistory.class));
}

private Item createItem(Integer itemId, Integer categoryId) {
Category category = new Category();
category.setId(categoryId);

Item item = new Item();
item.setId(itemId);
item.setDeleteFlag(Constant.NOT_DELETED);
item.setCategory(category);
return item;
}
}
