package com.cab.Service;

import com.cab.Exception.CabException;
import com.cab.Exception.CurrentUserSessionException;
import com.cab.Model.Cab;
import com.cab.Model.CurrentUserSession;
import com.cab.Repositary.CabRepo;
import com.cab.Repositary.CurrentUserSessionRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CabServiceImplTest {

    @Mock
    private CabRepo cabRepo;

    @Mock
    private CurrentUserSessionRepo currRepo;

    @InjectMocks
    private CabServiceImpl cabService;

    private Cab cab;
    private CurrentUserSession session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cab = new Cab();
        cab.setCarName("Toyota");
        cab.setCarNumber("ABC123");
        cab.setCarType("Sedan");
        cab.setPerKmRate(10);

        session = new CurrentUserSession();
        session.setUuid("valid-uuid");
        session.setCurrRole("Admin");
    }

    @Test
    void testInsertCab_CabNotExist_ShouldInsert() throws CabException {
        // Mock the repository call
        when(cabRepo.findByCarNumber("ABC123")).thenReturn(Optional.empty());
        when(cabRepo.save(cab)).thenReturn(cab);

        Cab insertedCab = cabService.insertCab(cab);

        assertNotNull(insertedCab);
        assertEquals("ABC123", insertedCab.getCarNumber());
    }

    @Test
    void testInsertCab_CabAlreadyExists_ShouldThrowException() {
        // Mock the repository call
        when(cabRepo.findByCarNumber("ABC123")).thenReturn(Optional.of(cab));

        assertThrows(CabException.class, () -> cabService.insertCab(cab));
    }

    @Test
    void testUpdateCab_UserNotAdmin_ShouldThrowException() {
        // Set the user role to something other than "Admin"
        CurrentUserSession session1 = null;

        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.ofNullable(session1));

        assertThrows(CurrentUserSessionException.class, () -> cabService.updateCab(cab, "valid-uuid"));
    }

    @Test
    void testUpdateCab_CabNotFound_ShouldThrowException() throws CurrentUserSessionException {
        // Mock user session and check if the cab exists
        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.of(session));
        when(cabRepo.findByCarNumber("ABC123")).thenReturn(Optional.empty());

        assertThrows(CabException.class, () -> cabService.updateCab(cab, "valid-uuid"));
    }

    @Test
    void testUpdateCab_CabExists_ShouldUpdate() throws CabException, CurrentUserSessionException {
        // Mock the repository calls
        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.of(session));
        when(cabRepo.findByCarNumber("ABC123")).thenReturn(Optional.of(cab));
        when(cabRepo.save(cab)).thenReturn(cab);

        Cab updatedCab = cabService.updateCab(cab, "valid-uuid");

        assertNotNull(updatedCab);
        assertEquals("Toyota", updatedCab.getCarName());
    }

    @Test
    void testDeleteCab_UserNotAdmin_ShouldThrowException() {
        // Set the user role to something other than "Admin"
        CurrentUserSession session1 = null;

        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.ofNullable(session1));

        assertThrows(CurrentUserSessionException.class, () -> cabService.deleteCab(1, "valid-uuid"));
    }

    @Test
    void testDeleteCab_CabNotFound_ShouldThrowException() throws CurrentUserSessionException {
        // Mock user session and check if the cab exists
        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.of(session));
        when(cabRepo.findById(1)).thenReturn(Optional.empty());

        assertThrows(CabException.class, () -> cabService.deleteCab(1, "valid-uuid"));
    }

    @Test
    void testDeleteCab_CabExists_ShouldDelete() throws CabException, CurrentUserSessionException {
        // Mock the repository calls
        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.of(session));
        when(cabRepo.findById(1)).thenReturn(Optional.of(cab));

        cabService.deleteCab(1, "valid-uuid");

        verify(cabRepo, times(1)).delete(cab);
    }

    @Test
    void testViewCabsOfType_UserNotAdmin_ShouldThrowException() {
        CurrentUserSession session1 = null;

        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.ofNullable(session1));

        assertThrows(CurrentUserSessionException.class, () -> cabService.viewCabsOfType("Sedan", "valid-uuid"));
    }

    @Test
    void testViewCabsOfType_CabsFound_ShouldReturnList() throws CabException, CurrentUserSessionException {
        // Mock user session
        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.of(session));

        List<Cab> cabList = new ArrayList<>();
        cabList.add(cab);

        when(cabRepo.findAll()).thenReturn(cabList);

        List<Cab> result = cabService.viewCabsOfType("Sedan", "valid-uuid");

        assertEquals(1, result.size());
        assertEquals("Sedan", result.get(0).getCarType());
    }

    @Test
    void testViewCabsOfType_NoCabsFound_ShouldThrowException() throws CurrentUserSessionException {
        // Mock user session
        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.of(session));

        when(cabRepo.findAll()).thenReturn(new ArrayList<>());

        assertThrows(CabException.class, () -> cabService.viewCabsOfType("Sedan", "valid-uuid"));
    }

    @Test
    void testCountCabsOfType_UserNotAdmin_ShouldThrowException() {
        CurrentUserSession session1 = null;

        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.ofNullable(session1));

        assertThrows(CurrentUserSessionException.class, () -> cabService.countCabsOfType("Sedan", "valid-uuid"));
    }

    @Test
    void testCountCabsOfType_CabsFound_ShouldReturnCount() throws CabException, CurrentUserSessionException {
        // Mock user session
        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.of(session));

        List<Cab> cabList = new ArrayList<>();
        cabList.add(cab);

        when(cabRepo.findAll()).thenReturn(cabList);

        int count = cabService.countCabsOfType("Sedan", "valid-uuid");

        assertEquals(1, count);
    }

    @Test
    void testCountCabsOfType_NoCabsFound_ShouldReturnZero() throws CurrentUserSessionException, CabException {
        // Mock user session
        when(currRepo.findByUuidAndRole("valid-uuid")).thenReturn(Optional.of(session));

        when(cabRepo.findAll()).thenReturn(new ArrayList<>());

        int count = cabService.countCabsOfType("Sedan", "valid-uuid");

        assertEquals(0, count);
    }
}
