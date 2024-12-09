package com.cab.Service;

import com.cab.Exception.CabException;
import com.cab.Exception.CurrentUserSessionException;
import com.cab.Exception.TripBookingException;
import com.cab.Model.Cab;
import com.cab.Model.CurrentUserSession;
import com.cab.Model.Customer;
import com.cab.Model.Driver;
import com.cab.Model.TripBooking;
import com.cab.Repositary.CabRepo;
import com.cab.Repositary.CurrentUserSessionRepo;
import com.cab.Repositary.CustomerRepo;
import com.cab.Repositary.DriverRepo;
import com.cab.Repositary.TripBookingRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class TripBookingServiceImplTest {

    @Mock
    private TripBookingRepo tripBookingRepo;

    @Mock
    private CustomerRepo customerRepo;

    @Mock
    private CabRepo cabRepo;

    @Mock
    private CurrentUserSessionRepo currRepo;

    @Mock
    private DriverRepo driverRepo;

    @InjectMocks
    private TripBookingServiceImpl tripBookingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSearchByLocation_Success() throws TripBookingException, CurrentUserSessionException {
        // Given
        String pickUpLocation = "Location1";
        String uuid = "valid-uuid";

        Cab availableCab = new Cab();
        availableCab.setCabCurrStatus("Available");
        availableCab.setCurrLocation(pickUpLocation);

        List<Cab> cabList = new ArrayList<>();
        cabList.add(availableCab);

        CurrentUserSession session = new CurrentUserSession();
        session.setUuid(uuid);

        when(currRepo.findByUuid(uuid)).thenReturn(Optional.of(session));
        when(cabRepo.findAll()).thenReturn(cabList);

        // When
        List<Cab> result = tripBookingService.searchByLocation(pickUpLocation, uuid);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Available", result.get(0).getCabCurrStatus());
    }

    @Test
    void testSearchByLocation_NoCabsAvailable() throws TripBookingException, CurrentUserSessionException {
        // Given
        String pickUpLocation = "Location1";
        String uuid = "valid-uuid";

        CurrentUserSession session = new CurrentUserSession();
        session.setUuid(uuid);

        when(currRepo.findByUuid(uuid)).thenReturn(Optional.of(session));
        when(cabRepo.findAll()).thenReturn(new ArrayList<>());

        // When & Then
        TripBookingException exception = assertThrows(TripBookingException.class, () ->
                tripBookingService.searchByLocation(pickUpLocation, uuid));
        assertEquals("No Cab Available in this Location", exception.getMessage());
    }

   // @Test
    void testBookRequest_Success() throws TripBookingException, CabException, CurrentUserSessionException {
        // Given
        String uuid = "valid-uuid";
        Integer cabId = 1;
        TripBooking tripBooking = new TripBooking();
        tripBooking.setFromDateTime("01-01-2024 10:00");
        tripBooking.setToDateTime("01-01-2024 12:00");
        tripBooking.setPickupLocation("Location1");

        CurrentUserSession session = new CurrentUserSession();
        session.setUuid(uuid);

        Cab cab = new Cab();
        cab.setCabCurrStatus("Available");
        cab.setCurrLocation("Location1");

        Customer customer = new Customer();
        customer.setCustomerId(1123);
        Optional<Customer> mockedCustomer = Optional.of(customer);
        when(currRepo.findByUuid(uuid)).thenReturn(Optional.of(session));
        when(customerRepo.findById(1)).thenReturn(mockedCustomer);
        when(cabRepo.findById(cabId)).thenReturn(Optional.of(cab));

        // When
        TripBooking result = tripBookingService.BookRequest(cabId, tripBooking, uuid);

        // Then
        assertNotNull(result);
        assertEquals("Pending", result.getCurrStatus());
        assertEquals("Pending", cab.getCabCurrStatus());
    }


   // @Test
    void testBookRequest_CabNotAvailable() throws TripBookingException, CabException, CurrentUserSessionException {
        // Given
        String uuid = "valid-uuid";
        Integer cabId = 1;
        TripBooking tripBooking = new TripBooking();
        tripBooking.setFromDateTime("01-01-2024 10:00");
        tripBooking.setToDateTime("01-01-2024 12:00");
        tripBooking.setPickupLocation("Location1");

        CurrentUserSession session = new CurrentUserSession();
        session.setUuid(uuid);

        Cab cab = new Cab();
        cab.setCabCurrStatus("Booked");
        cab.setCurrLocation("Location1");

        Optional<Customer> customer = Optional.of(new Customer());
        customer.get().setCustomerId(1);

        when(currRepo.findByUuid(uuid)).thenReturn(Optional.of(session));
        when(customerRepo.findById(1)).thenReturn(customer);
        when(cabRepo.findById(cabId)).thenReturn(Optional.of(cab));

        // When & Then
        CabException exception = assertThrows(CabException.class, () ->
                tripBookingService.BookRequest(cabId, tripBooking, uuid));
        assertEquals("This Cab is not available currently for location or avability purpose", exception.getMessage());
    }

  //  @Test
    void testAssignDriverByAdmin_Success() throws TripBookingException, CabException, CurrentUserSessionException {
        // Given
        Integer tripBookingId = 1;
        String uuid = "admin-uuid";

        TripBooking tripBooking = new TripBooking();
        tripBooking.setTripBookingId(tripBookingId);
        tripBooking.setPickupLocation("Location1");

        Driver driver = new Driver();
        driver.setCurrDriverStatus("available");

        Cab cab = new Cab();
        cab.setCabCurrStatus("Available");

        CurrentUserSession session = new CurrentUserSession();
        session.setUuid(uuid);
        session.setCurrRole("admin");

        when(currRepo.findByUuidAndRole(uuid)).thenReturn(Optional.of(session));
        when(tripBookingRepo.findById(tripBookingId)).thenReturn(Optional.of(tripBooking));
        when(driverRepo.findByCurrLocationAndCurrDriverStatus("Location1", "available")).thenReturn(Collections.singletonList(driver));
        when(cabRepo.save(any(Cab.class))).thenReturn(cab);

        // When
        TripBooking result = tripBookingService.AssignDriverByAdmin(tripBookingId, uuid);

        // Then
        assertNotNull(result);
        assertEquals("confirmed", result.getCurrStatus());
        assertEquals(driver, result.getDriver());
    }
}
