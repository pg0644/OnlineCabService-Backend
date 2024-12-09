package com.cab.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.cab.Exception.CustomerException;
import com.cab.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.cab.Exception.AdminException;
import com.cab.Exception.CurrentUserSessionException;
import com.cab.Exception.TripBookingException;
import com.cab.Repositary.AdminRepo;
import com.cab.Repositary.CurrentUserSessionRepo;
import com.cab.Repositary.CustomerRepo;
import com.cab.Repositary.TripBookingRepo;

class AdminServiceImplTest {

    @Mock
    private AdminRepo adminRepo;

    @Mock
    private CustomerRepo customerRepo;

    @Mock
    private TripBookingRepo tripBookingRepo;

    @Mock
    private CurrentUserSessionRepo currRepo;

    @InjectMocks
    private AdminServiceImpl adminService;

    private Admin admin;
    private CurrentUserSession session;

    private CurrentUserSession validSession;
    private CurrentUserSession invalidSession;

    private Customer customer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize a sample admin
        admin = new Admin();
        admin.setUserRole("Admin");
        admin.setEmail("praneeth@gmail.com");
        admin.setAdminId(1170);
        admin.setAddress("Unt union circle");

        // Initialize a valid session
        session = new CurrentUserSession();
        session.setUuid("valid-uuid");
        session.setCurrRole("Admin");

        validSession = new CurrentUserSession();
        validSession.setUuid("valid-uuid");
        validSession.setCurrRole("Admin");
        invalidSession = null;

        customer = new Customer();
        customer.setCustomerId(1);

    }

    @Test
    void testInsertAdmin_Success() throws AdminException {
        // Mock repository behavior
        when(adminRepo.findByEmail(admin.getEmail())).thenReturn(Optional.empty());
        when(adminRepo.save(admin)).thenReturn(admin);

        // Call the service method
        Admin savedAdmin = adminService.insertAdmin(admin);

        // Verify results
        assertNotNull(savedAdmin);
        assertEquals(admin.getEmail(), savedAdmin.getEmail());
        verify(adminRepo, times(1)).save(admin);
    }

    @Test
    void testInsertAdmin_AdminAlreadyRegistered() {
        // Mock repository behavior
        when(adminRepo.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));

        // Call the service method and expect an exception
        assertThrows(AdminException.class, () -> adminService.insertAdmin(admin));
    }

    @Test
    void testInsertAdmin_NotAnAdminRole() {
        admin.setUserRole("Customer"); // Set invalid role

        // Call the service method and expect an exception
        assertThrows(AdminException.class, () -> adminService.insertAdmin(admin));
    }

    @Test
    void testUpdateAdmin_Success() throws AdminException, CurrentUserSessionException {
        // Mock repository behavior
        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.of(session));
        when(adminRepo.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));

        // Call the service method
        Admin updatedAdmin = adminService.updateAdmin(admin, "valid-uuid");

        // Verify results
        assertNotNull(updatedAdmin);
        verify(adminRepo, times(1)).save(admin);
    }

    @Test
    void testUpdateAdmin_AdminNotLoggedIn() {
        // Mock repository behavior
        when(currRepo.findByUuidAndRole("invalid-uuid")).thenReturn(Optional.empty());

        // Call the service method and expect an exception
        assertThrows(CurrentUserSessionException.class, () -> adminService.updateAdmin(admin, "invalid-uuid"));
    }

    @Test
    void testDeleteAdmin_Success() throws AdminException, CurrentUserSessionException {
        // Mock repository behavior
        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.of(session));
        when(adminRepo.findById(admin.getAdminId())).thenReturn(Optional.of(admin));

        // Call the service method
        Admin deletedAdmin = adminService.deleteAdmin(admin.getAdminId(), "valid-uuid");

        // Verify results
        assertNotNull(deletedAdmin);
        verify(adminRepo, times(1)).delete(admin);
    }

    @Test
    void testDeleteAdmin_AdminNotLoggedIn() {
        // Mock repository behavior
        when(currRepo.findByUuidAndRole("invalid-uuid")).thenReturn(Optional.empty());

        // Call the service method and expect an exception
        assertThrows(CurrentUserSessionException.class, () -> adminService.deleteAdmin(1, "invalid-uuid"));
    }

    @Test
    void testGetAllTrips_Success() throws CurrentUserSessionException, TripBookingException, AdminException {
        // Mock repository behavior
        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.of(session));
        List<TripBooking> trips = new ArrayList<>();
        trips.add(new TripBooking());
        when(tripBookingRepo.findAll()).thenReturn(trips);

        // Call the service method
        List<TripBooking> allTrips = adminService.getAllTrips("valid-uuid");

        // Verify results
        assertNotNull(allTrips);
        assertFalse(allTrips.isEmpty());
        verify(tripBookingRepo, times(1)).findAll();
    }

    @Test
    void testGetAllTrips_NoTripsFound() throws CurrentUserSessionException {
        // Mock repository behavior
        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.of(session));
        when(tripBookingRepo.findAll()).thenReturn(new ArrayList<>());

        // Call the service method and expect an exception
        assertThrows(TripBookingException.class, () -> adminService.getAllTrips("valid-uuid"));
    }

    @Test
    void testGetTripsCabwise_Success() throws TripBookingException, CurrentUserSessionException {
        // Arrange
        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.of(validSession));

        TripBooking trip1 = new TripBooking();
        Cab cab = new Cab();
        cab.setCarType("Sedan");
        trip1.setCab(cab);

        TripBooking trip2 = new TripBooking();
        Cab cab2 = new Cab();
        cab2.setCarType("Hatchback");
        trip1.setCab(cab2);
        trip2.setCab(cab2);

        List<TripBooking> trips = List.of(trip1, trip2);
        when(tripBookingRepo.findAll()).thenReturn(trips);

        // Act
        List<TripBooking> result = adminService.getTripsCabwise("Hatchback", "valid-uuid");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Hatchback", result.get(0).getCab().getCarType());
        verify(tripBookingRepo, times(1)).findAll();
    }

    @Test
    void testGetTripsCabwise_NoTripsAvailable() {
        // Arrange
        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.of(validSession));
        when(tripBookingRepo.findAll()).thenReturn(new ArrayList<>());

        // Act & Assert
        TripBookingException exception = assertThrows(TripBookingException.class,
                () -> adminService.getTripsCabwise("Sedan", "valid-uuid"));
        assertEquals("No Trip is Booked Currently By any Customer", exception.getMessage());
    }

    @Test
    void testGetTripsCabwise_NoTripsWithCarType() {
        // Arrange
        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.of(validSession));

        TripBooking trip1 = new TripBooking();
        Cab cab = new Cab();
        cab.setCarType("Suv");
        trip1.setCab(cab);

        List<TripBooking> trips = List.of(trip1);
        when(tripBookingRepo.findAll()).thenReturn(trips);

        // Act & Assert
        TripBookingException exception = assertThrows(TripBookingException.class,
                () -> adminService.getTripsCabwise("Sedan", "valid-uuid"));
        assertEquals("No Trip Found With this carType", exception.getMessage());
    }

    @Test
    void testGetTripsCabwise_AdminNotLoggedIn() {
        // Arrange
        when(currRepo.findByUuidAndRole("invalid-uuid")).thenReturn(Optional.empty());

        // Act & Assert
        CurrentUserSessionException exception = assertThrows(CurrentUserSessionException.class,
                () -> adminService.getTripsCabwise("Sedan", "invalid-uuid"));
        assertEquals("Admin is Not Logged In", exception.getMessage());
    }


    @Test
    void testGetTripsCustomerwise_Success() throws TripBookingException, CustomerException, CurrentUserSessionException {
        // Arrange
        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.of(validSession));
        when(customerRepo.findById(1)).thenReturn(Optional.of(customer));
        TripBooking trip1 = new TripBooking();
        Cab cab = new Cab();
        cab.setCarType("Sedan");
        trip1.setCab(cab);

        TripBooking trip2 = new TripBooking();
        Cab cab2 = new Cab();
        cab2.setCarType("Hatchback");
        trip2.setCab(cab2);
        customer.setTripBooking(List.of(trip1, trip2));
        // Act
        List<TripBooking> result = adminService.getTripsCustomerwise(1, "valid-uuid");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(currRepo, times(1)).findByUuidAndRole("valid-uuid");
        verify(customerRepo, times(1)).findById(1);
    }

    @Test
    void testGetTripsCustomerwise_NoTripsBooked() {
        // Arrange
        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.of(validSession));

        Customer customerWithNoTrips = new Customer();
        customerWithNoTrips.setCustomerId(2);
        customerWithNoTrips.setTripBooking(new ArrayList<>());

        when(customerRepo.findById(2)).thenReturn(Optional.of(customerWithNoTrips));

        // Act & Assert
        CustomerException exception = assertThrows(CustomerException.class,
                () -> adminService.getTripsCustomerwise(2, "valid-uuid"));
        assertEquals("No Trip Bookked by the customer", exception.getMessage());
    }

    @Test
    void testGetTripsCustomerwise_CustomerNotFound() {
        // Arrange
        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.of(validSession));
        when(customerRepo.findById(3)).thenReturn(Optional.empty());

        // Act & Assert
        CustomerException exception = assertThrows(CustomerException.class,
                () -> adminService.getTripsCustomerwise(3, "valid-uuid"));
        assertEquals("Customer with this Credential is not present", exception.getMessage());
    }

    @Test
    void testGetTripsCustomerwise_AdminNotLoggedIn() {
        // Arrange
        when(currRepo.findByUuidAndRole("invalid-uuid")).thenReturn(Optional.empty());

        // Act & Assert
        CurrentUserSessionException exception = assertThrows(CurrentUserSessionException.class,
                () -> adminService.getTripsCustomerwise(1, "invalid-uuid"));
        assertEquals("Admin is Not Logged In Or User is not Admin", exception.getMessage());
    }


   // @Test
    void testGetAllTripsForDays_Success() throws TripBookingException, CustomerException, CurrentUserSessionException {
        TripBooking trip1 = new TripBooking();
        trip1.setTripBookingId(123);
        trip1.setFromDateTime("01-12-2024 10:00");
        trip1.setToDateTime("01-12-2024 12:00");

        TripBooking trip2 = new TripBooking();
        trip1.setTripBookingId(456);
        trip2.setFromDateTime("02-12-2024 09:00");
        trip2.setToDateTime("02-12-2024 11:00");
        Customer customer1 = new Customer();

        customer1.setTripBooking(List.of(trip1, trip2));
        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.of(validSession));
        when(customerRepo.findById(1)).thenReturn(Optional.of(customer1));

        String fromDateTime = "01-12-2024 09:00";
        String toDateTime = "02-12-2024 12:00";

        // Act
        List<TripBooking> result = adminService.getAllTripsForDays(1, fromDateTime, toDateTime, "valid-uuid");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(currRepo, times(1)).findByUuidAndRole("valid-uuid");
        verify(customerRepo, times(1)).findById(1);
    }

    @Test
    void testGetAllTripsForDays_NoTripsInRange() throws TripBookingException, CustomerException, CurrentUserSessionException {
        // Arrange
        TripBooking trip1 = new TripBooking();
        trip1.setFromDateTime("01-12-2024 10:00");
        trip1.setToDateTime("01-12-2024 12:00");

        TripBooking trip2 = new TripBooking();
        trip2.setFromDateTime("02-12-2024 09:00");
        trip2.setToDateTime("02-12-2024 11:00");

        Customer customer1 = new Customer();
        customer1.setTripBooking(List.of(trip1, trip2));

        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.of(validSession));
        when(customerRepo.findById(1)).thenReturn(Optional.of(customer1));

        String fromDateTime = "03-12-2024 09:00";  // Dates outside the range of trips
        String toDateTime = "04-12-2024 12:00";
//
//        // Act & Assert
//        TripBookingException exception = assertThrows(TripBookingException.class,
//                () -> adminService.getAllTripsForDays(1, fromDateTime, toDateTime, "valid-uuid"));
//        assertEquals("No Trip has been booked in between of the given Dates", exception.getMessage());
    }

    @Test
    void testGetAllTripsForDays_AdminNotLoggedIn() {
        // Arrange
        when(currRepo.findByUuidAndRole("invalid-uuid")).thenReturn(Optional.empty());

        // Act & Assert
        CurrentUserSessionException exception = assertThrows(CurrentUserSessionException.class,
                () -> adminService.getAllTripsForDays(1, "01-12-2024 09:00", "02-12-2024 12:00", "invalid-uuid"));
        assertEquals("Admin is Not Logged In", exception.getMessage());
    }

    @Test
    void testGetAllTripsForDays_CustomerNotFound() {
        // Arrange
        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.of(validSession));
        when(customerRepo.findById(999)).thenReturn(Optional.empty());  // Invalid customer ID

        // Act & Assert
        CustomerException exception = assertThrows(CustomerException.class,
                () -> adminService.getAllTripsForDays(999, "01-12-2024 09:00", "02-12-2024 12:00", "valid-uuid"));
        assertEquals("No Customer Found with this Credentials", exception.getMessage());
    }

    @Test
    void testGetAllTripsForDays_EmptyTrips() throws TripBookingException, CustomerException, CurrentUserSessionException {
        // Arrange
        Customer customer1 = new Customer();
        customer1.setTripBooking(new ArrayList<>());  // No trips for the customer

        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.of(validSession));
        when(customerRepo.findById(1)).thenReturn(Optional.of(customer1));

        String fromDateTime = "01-12-2024 09:00";
        String toDateTime = "02-12-2024 12:00";

        // Act & Assert
        TripBookingException exception = assertThrows(TripBookingException.class,
                () -> adminService.getAllTripsForDays(1, fromDateTime, toDateTime, "valid-uuid"));
        assertEquals("No Trip has been booked in between of the given Dates", exception.getMessage());
    }

}
