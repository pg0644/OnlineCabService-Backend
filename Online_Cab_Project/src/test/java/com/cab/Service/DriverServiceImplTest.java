package com.cab.Service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.cab.Exception.CurrentUserSessionException;
import com.cab.Exception.DriverException;
import com.cab.Model.CurrentUserSession;
import com.cab.Model.Driver;
import com.cab.Repositary.CurrentUserSessionRepo;
import com.cab.Repositary.DriverRepo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

public class DriverServiceImplTest {

    @Mock
    private DriverRepo driverRepo;

    @Mock
    private CurrentUserSessionRepo currRepo;

    @InjectMocks
    private DriverServiceImpl driverService;

    private Driver driver;
    private CurrentUserSession currentUserSession;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        driver = new Driver();
        driver.setEmail("test@driver.com");
        driver.setLicenceNo("ABC123");
        driver.setUserRole("Driver");

        currentUserSession = new CurrentUserSession();
        currentUserSession.setUuid("12345");
    }

    // Test insertDriver method
    @Test
    public void testInsertDriver_Success() throws DriverException {
        when(driverRepo.findByLicenceNo(driver.getLicenceNo())).thenReturn(Optional.empty());
        when(driverRepo.save(driver)).thenReturn(driver);

        Driver savedDriver = driverService.insertDriver(driver);
        assertNotNull(savedDriver);
        assertEquals(driver.getEmail(), savedDriver.getEmail());
    }

    @Test
    public void testInsertDriver_AlreadyExists() {
        when(driverRepo.findByLicenceNo(driver.getLicenceNo())).thenReturn(Optional.of(driver));

        DriverException exception = assertThrows(DriverException.class, () -> {
            driverService.insertDriver(driver);
        });
        assertEquals("Driver is already registered", exception.getMessage());
    }

    @Test
    public void testInsertDriver_InvalidRole() {
        driver.setUserRole("Admin");
        when(driverRepo.findByLicenceNo(driver.getLicenceNo())).thenReturn(Optional.empty());

        DriverException exception = assertThrows(DriverException.class, () -> {
            driverService.insertDriver(driver);
        });
        assertEquals("User is not a Driver", exception.getMessage());
    }

    // Test updateDriver method
    @Test
    public void testUpdateDriver_Success() throws DriverException, CurrentUserSessionException {
        when(currRepo.findByUuid("12345")).thenReturn(Optional.of(currentUserSession));
        when(driverRepo.findByEmail(driver.getEmail())).thenReturn(Optional.of(driver));
        when(driverRepo.save(driver)).thenReturn(driver);

        Driver updatedDriver = driverService.updateDriver(driver, "12345");
        assertNotNull(updatedDriver);
        assertEquals(driver.getEmail(), updatedDriver.getEmail());
    }

    @Test
    public void testUpdateDriver_SessionNotFound() {
        when(currRepo.findByUuid("12345")).thenReturn(Optional.empty());

        CurrentUserSessionException exception = assertThrows(CurrentUserSessionException.class, () -> {
            driverService.updateDriver(driver, "12345");
        });
        assertEquals("User is Not Logged In", exception.getMessage());
    }

    @Test
    public void testUpdateDriver_DriverNotFound() throws CurrentUserSessionException {
        when(currRepo.findByUuid("12345")).thenReturn(Optional.of(currentUserSession));
        when(driverRepo.findByEmail(driver.getEmail())).thenReturn(Optional.empty());

        DriverException exception = assertThrows(DriverException.class, () -> {
            driverService.updateDriver(driver, "12345");
        });
        assertEquals("Driver not found with this Credentials", exception.getMessage());
    }

    // Test deleteDriver method
    @Test
    public void testDeleteDriver_Success() throws DriverException, CurrentUserSessionException {
        when(currRepo.findByUuid("12345")).thenReturn(Optional.of(currentUserSession));
        when(driverRepo.findById(1)).thenReturn(Optional.of(driver));

        Driver deletedDriver = driverService.deleteDriver(1, "12345");
        assertNotNull(deletedDriver);
        verify(driverRepo, times(1)).delete(driver);
    }

    @Test
    public void testDeleteDriver_SessionNotFound() {
        when(currRepo.findByUuid("12345")).thenReturn(Optional.empty());

        CurrentUserSessionException exception = assertThrows(CurrentUserSessionException.class, () -> {
            driverService.deleteDriver(1, "12345");
        });
        assertEquals("User is Not Logged In", exception.getMessage());
    }

    @Test
    public void testDeleteDriver_DriverNotFound() throws CurrentUserSessionException {
        when(currRepo.findByUuid("12345")).thenReturn(Optional.of(currentUserSession));
        when(driverRepo.findById(1)).thenReturn(Optional.empty());

        DriverException exception = assertThrows(DriverException.class, () -> {
            driverService.deleteDriver(1, "12345");
        });
        assertEquals("Driver not found with this Credentials", exception.getMessage());
    }

    // Test viewBestDriver method
    @Test
    public void testViewBestDriver_Success() throws DriverException, CurrentUserSessionException {
        Driver driver1 = new Driver();
        driver1.setRating(4.7f);
        Driver driver2 = new Driver();
        driver2.setRating(4.9f);
        Driver driver3 = new Driver();
        driver3.setRating(4.0f);

        when(currRepo.findByUuid("12345")).thenReturn(Optional.of(currentUserSession));
        when(driverRepo.findAll()).thenReturn(List.of(driver1, driver2, driver3));

        List<Driver> bestDrivers = driverService.viewBestDriver("12345");
        assertEquals(2, bestDrivers.size());
        assertTrue(bestDrivers.get(0).getRating() >= bestDrivers.get(1).getRating());
    }

    @Test
    public void testViewBestDriver_NoBestDrivers() throws DriverException, CurrentUserSessionException {
        when(currRepo.findByUuid("12345")).thenReturn(Optional.of(currentUserSession));
        when(driverRepo.findAll()).thenReturn(List.of());

        DriverException exception = assertThrows(DriverException.class, () -> {
            driverService.viewBestDriver("12345");
        });
        assertEquals("No Best Driver Present", exception.getMessage());
    }

    // Test viewDriver method
    @Test
    public void testViewDriver_Success() throws DriverException, CurrentUserSessionException {
        when(currRepo.findByUuid("12345")).thenReturn(Optional.of(currentUserSession));
        when(driverRepo.findById(1)).thenReturn(Optional.of(driver));

        Driver foundDriver = driverService.viewDriver(1, "12345");
        assertNotNull(foundDriver);
        assertEquals(driver.getEmail(), foundDriver.getEmail());
    }

    @Test
    public void testViewDriver_SessionNotFound() {
        when(currRepo.findByUuid("12345")).thenReturn(Optional.empty());

        CurrentUserSessionException exception = assertThrows(CurrentUserSessionException.class, () -> {
            driverService.viewDriver(1, "12345");
        });
        assertEquals("User is Not Logged In", exception.getMessage());
    }

    @Test
    public void testViewDriver_DriverNotFound() throws CurrentUserSessionException {
        when(currRepo.findByUuid("12345")).thenReturn(Optional.of(currentUserSession));
        when(driverRepo.findById(1)).thenReturn(Optional.empty());

        DriverException exception = assertThrows(DriverException.class, () -> {
            driverService.viewDriver(1, "12345");
        });
        assertEquals("Driver not found with this Credentials", exception.getMessage());
    }
}
