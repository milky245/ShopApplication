package jp.co.sss.shop.controller.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;

import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.LoginHistory;
import jp.co.sss.shop.form.LoginForm;
import jp.co.sss.shop.repository.LoginHistoryRepository;
import jp.co.sss.shop.repository.UserRepository;

public class LoginControllerHistoryTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoginHistoryRepository loginHistoryRepository;

    @Mock
    private HttpSession session;

    @InjectMocks
    private LoginController loginController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testDoLogin_RecordsHistoryOnSuccess() throws Exception {
        // Arrange
        LoginForm form = new LoginForm();
        form.setEmail("test@example.com");
        form.setPassword("password");

        UserBean userBean = new UserBean();
        userBean.setId(1);
        userBean.setAuthority(2); // Client

        when(session.getAttribute("user")).thenReturn(userBean);
        BindingResult result = org.mockito.Mockito.mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");

        // Act & Assert
        String returnPath = loginController.doLogin(form, result, request);

        assertEquals("redirect:/", returnPath);
        verify(loginHistoryRepository, times(1)).save(any(LoginHistory.class));
    }
}
