package com.cab.Service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.cab.Exception.CustomerException;
import com.cab.Exception.CurrentUserSessionException;
import com.cab.Model.CurrentUserSession;
import com.cab.Model.Customer;
import com.cab.Repositary.CurrentUserSessionRepo;
import com.cab.Repositary.CustomerRepo;

public class CustomerServiceImplTest {

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Mock
    private CustomerRepo customerRepo;

    @Mock
    private CurrentUserSessionRepo currRepo;

    private Customer validCustomer;
    private CurrentUserSession validSession;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        validCustomer = new Customer();
        validCustomer.setEmail("customer@example.com");
        validCustomer.setUserName("customer");
        validCustomer.setUserRole("Customer");

        validSession = new CurrentUserSession();
        validSession.setUuid("valid-uuid");
    }

    @Test
    void testInsertCustomer_Success() throws CustomerException {
        when(customerRepo.findByEmail(validCustomer.getEmail())).thenReturn(Optional.empty());
        when(customerRepo.save(validCustomer)).thenReturn(validCustomer);

        Customer result = customerService.insertCustomer(validCustomer);

        assertNotNull(result);
        assertEquals(validCustomer.getEmail(), result.getEmail());
        verify(customerRepo, times(1)).save(validCustomer);
    }

    @Test
    void testInsertCustomer_AlreadyRegistered() throws CustomerException {
        when(customerRepo.findByEmail(validCustomer.getEmail())).thenReturn(Optional.of(validCustomer));

        CustomerException exception = assertThrows(CustomerException.class, () -> {
            customerService.insertCustomer(validCustomer);
        });

        assertEquals("Customer is Already Registered", exception.getMessage());
    }

    @Test
    void testInsertCustomer_InvalidRole() {
        Customer customer = new Customer();
        customer.setEmail("test@example.com");
        customer.setUserRole("Admin");  // Invalid role

        CustomerException exception = assertThrows(CustomerException.class, () -> {
            customerService.insertCustomer(customer);
        });

        assertEquals("The User is not a Customer", exception.getMessage());
    }


    @Test
    void testUpdateCustomer_Success() throws CustomerException, CurrentUserSessionException {
        when(currRepo.findByUuid("valid-uuid")).thenReturn(Optional.of(validSession));
        when(customerRepo.findByEmail(validCustomer.getEmail())).thenReturn(Optional.of(validCustomer));
        when(customerRepo.save(validCustomer)).thenReturn(validCustomer);

        Customer updatedCustomer = customerService.updateCustomer(validCustomer, "valid-uuid");

        assertNotNull(updatedCustomer);
        verify(customerRepo, times(1)).save(validCustomer);
    }

    @Test
    void testUpdateCustomer_CustomerNotFound() throws CustomerException, CurrentUserSessionException {
        when(currRepo.findByUuid("valid-uuid")).thenReturn(Optional.of(validSession));
        when(customerRepo.findByEmail(validCustomer.getEmail())).thenReturn(Optional.empty());

        CustomerException exception = assertThrows(CustomerException.class, () -> {
            customerService.updateCustomer(validCustomer, "valid-uuid");
        });

        assertEquals("Customer not found with this Credentials", exception.getMessage());
    }

    @Test
    void testUpdateCustomer_NotLoggedIn() throws CustomerException, CurrentUserSessionException {
        when(currRepo.findByUuid("invalid-uuid")).thenReturn(Optional.empty());

        CurrentUserSessionException exception = assertThrows(CurrentUserSessionException.class, () -> {
            customerService.updateCustomer(validCustomer, "invalid-uuid");
        });

        assertEquals("Customer is Not Logged In", exception.getMessage());
    }

    @Test
    void testDeleteCustomer_Success() throws CustomerException, CurrentUserSessionException {
        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.of(validSession));
        when(customerRepo.findById(1)).thenReturn(Optional.of(validCustomer));

        Customer deletedCustomer = customerService.deleteCustomer(1, "valid-uuid");

        assertNotNull(deletedCustomer);
        verify(customerRepo, times(1)).delete(validCustomer);
    }

    @Test
    void testDeleteCustomer_CustomerNotFound() throws CustomerException, CurrentUserSessionException {
        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.of(validSession));
        when(customerRepo.findById(1)).thenReturn(Optional.empty());

        CustomerException exception = assertThrows(CustomerException.class, () -> {
            customerService.deleteCustomer(1, "valid-uuid");
        });

        assertEquals("Customer not found with this details", exception.getMessage());
    }

    @Test
    void testDeleteCustomer_NotLoggedIn() throws CustomerException, CurrentUserSessionException {
        when(currRepo.findByUuidAndRole("invalid-uuid")).thenReturn(Optional.empty());

        CurrentUserSessionException exception = assertThrows(CurrentUserSessionException.class, () -> {
            customerService.deleteCustomer(1, "invalid-uuid");
        });

        assertEquals("Customer is Not Logged In", exception.getMessage());
    }

    @Test
    void testViewCustomer_Success() throws CustomerException, CurrentUserSessionException {
        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.of(validSession));
        when(customerRepo.findAll()).thenReturn(List.of(validCustomer));

        List<Customer> customers = customerService.viewCustomer("valid-uuid");

        assertNotNull(customers);
        assertFalse(customers.isEmpty());
    }

    @Test
    void testViewCustomer_NoCustomers() throws CustomerException, CurrentUserSessionException {
        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.of(validSession));
        when(customerRepo.findAll()).thenReturn(List.of());

        CustomerException exception = assertThrows(CustomerException.class, () -> {
            customerService.viewCustomer("valid-uuid");
        });

        assertEquals("No Customer present", exception.getMessage());
    }

    @Test
    void testViewCustomer_NotLoggedIn() throws CustomerException, CurrentUserSessionException {
        when(currRepo.findByUuidAndRole("invalid-uuid")).thenReturn(Optional.empty());

        CurrentUserSessionException exception = assertThrows(CurrentUserSessionException.class, () -> {
            customerService.viewCustomer("invalid-uuid");
        });

        assertEquals("Customer is Not Logged In", exception.getMessage());
    }

    @Test
    void testViewCustomerById_Success() throws CustomerException, CurrentUserSessionException {
        when(currRepo.findByUuid("valid-uuid")).thenReturn(Optional.of(validSession));
        when(customerRepo.findById(1)).thenReturn(Optional.of(validCustomer));

        Customer customer = customerService.viewCustomer(1, "valid-uuid");

        assertNotNull(customer);
    }

    @Test
    void testViewCustomerById_CustomerNotFound() throws CustomerException, CurrentUserSessionException {
        when(currRepo.findByUuid("valid-uuid")).thenReturn(Optional.of(validSession));
        when(customerRepo.findById(1)).thenReturn(Optional.empty());

        CustomerException exception = assertThrows(CustomerException.class, () -> {
            customerService.viewCustomer(1, "valid-uuid");
        });

        assertEquals("Customer not found with this details", exception.getMessage());
    }

    @Test
    void testViewCustomerById_NotLoggedIn() throws CustomerException, CurrentUserSessionException {
        when(currRepo.findByUuid("invalid-uuid")).thenReturn(Optional.empty());

        CurrentUserSessionException exception = assertThrows(CurrentUserSessionException.class, () -> {
            customerService.viewCustomer(1, "invalid-uuid");
        });

        assertEquals("Customer is Not Logged In", exception.getMessage());
    }
}
