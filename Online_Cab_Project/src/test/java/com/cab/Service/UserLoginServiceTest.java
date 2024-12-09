package com.cab.Service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.cab.Exception.AdminException;
import com.cab.Exception.CustomerException;
import com.cab.Exception.CurrentUserSessionException;
import com.cab.Model.Admin;
import com.cab.Model.Customer;
import com.cab.Model.CurrentUserSession;
import com.cab.Model.UserLoginDTO;
import com.cab.Repositary.AdminRepo;
import com.cab.Repositary.CurrentUserSessionRepo;
import com.cab.Repositary.CustomerRepo;

import java.util.Optional;

public class UserLoginServiceTest {

    @Mock
    private CustomerRepo customerRepo;

    @Mock
    private AdminRepo adminRepo;

    @Mock
    private CurrentUserSessionRepo currRepo;

    @InjectMocks
    private UserLoginServiceimpl userLoginService;

    private UserLoginDTO loginDto;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        loginDto = new UserLoginDTO();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("password123");
    }

    // Test for Admin Login Success
 //   @Test
    public void testAdminLoginSuccess() throws AdminException, CustomerException {
        Admin admin = new Admin();
        admin.setAdminId(1);
        admin.setEmail("test@example.com");
        admin.setPassword("password123");

        when(adminRepo.findByEmail("test@example.com")).thenReturn(Optional.of(admin));
        when(customerRepo.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(currRepo.findById(1)).thenReturn(Optional.empty());

        CurrentUserSession session = userLoginService.login(loginDto);

        assertNotNull(session);
        assertEquals("Admin", session.getCurrRole());
        assertEquals("Login Successfull", session.getCurrStatus());
    }

    // Test for Admin Login - Admin Already Logged In
    @Test
    public void testAdminLoginAlreadyLoggedIn() throws AdminException, CustomerException {
        Admin admin = new Admin();
        admin.setAdminId(1);
        admin.setEmail("test@example.com");
        admin.setPassword("password123");

        when(adminRepo.findByEmail("test@example.com")).thenReturn(Optional.of(admin));
        when(customerRepo.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(currRepo.findById(1)).thenReturn(Optional.of(new CurrentUserSession()));

        AdminException exception = assertThrows(AdminException.class, () -> {
            userLoginService.login(loginDto);
        });

        assertEquals("Admin is currently Login Please Logout and then try", exception.getMessage());
    }

    // Test for Customer Login Success
 //   @Test
    public void testCustomerLoginSuccess() throws AdminException, CustomerException {
        Customer customer = new Customer();
        customer.setCustomerId(1);
        customer.setEmail("test@example.com");
        customer.setPassword("password123");

        when(adminRepo.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());
        when(customerRepo.findByEmail(Mockito.anyString())).thenReturn(Optional.of(customer));
        when(currRepo.findById(Mockito.anyInt())).thenReturn(Optional.empty());

        CurrentUserSession session = userLoginService.login(loginDto);

        assertNotNull(session);
        assertEquals("Customer", session.getCurrRole());
        assertEquals("Login Successfull", session.getCurrStatus());
    }

    // Test for Customer Login - Customer Already Logged In
    @Test
    public void testCustomerLoginAlreadyLoggedIn() throws AdminException, CustomerException {
        Customer customer = new Customer();
        customer.setCustomerId(1);
        customer.setEmail("test@example.com");
        customer.setPassword("password123");

        when(adminRepo.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(customerRepo.findByEmail("test@example.com")).thenReturn(Optional.of(customer));
        when(currRepo.findById(1)).thenReturn(Optional.of(new CurrentUserSession()));

        CustomerException exception = assertThrows(CustomerException.class, () -> {
            userLoginService.login(loginDto);
        });

        assertEquals("Customer is currently Login Please Logout and then try", exception.getMessage());
    }

    // Test for Invalid Login (Not Registered User)
    @Test
    public void testInvalidLogin() throws AdminException, CustomerException {
        when(adminRepo.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(customerRepo.findByEmail("test@example.com")).thenReturn(Optional.empty());

        CustomerException exception = assertThrows(CustomerException.class, () -> {
            userLoginService.login(loginDto);
        });

        assertEquals("User is Not Registered", exception.getMessage());
    }

    // Test for Logout Success
    @Test
    public void testLogoutSuccess() throws CurrentUserSessionException {
        CurrentUserSession session = new CurrentUserSession();
        session.setUuid("some-uuid");

        when(currRepo.findByUuid("some-uuid")).thenReturn(Optional.of(session));

        String result = userLoginService.LogOut("some-uuid");

        assertEquals("User Logged Out Successfully", result);
    }

    // Test for Logout - User Not Logged In
    @Test
    public void testLogoutUserNotLoggedIn() throws CurrentUserSessionException {
        when(currRepo.findByUuid("some-uuid")).thenReturn(Optional.empty());

        CurrentUserSessionException exception = assertThrows(CurrentUserSessionException.class, () -> {
            userLoginService.LogOut("some-uuid");
        });

        assertEquals("User Not Logged In with this Credentials", exception.getMessage());
    }
}
