package id.ac.ui.cs.advprog.groupproject.auth.service;

import id.ac.ui.cs.advprog.groupproject.auth.model.*;
import id.ac.ui.cs.advprog.groupproject.auth.repository.KycRequestRepository;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KycRequestServiceTest {
    @Mock
    private KycRequestRepository kycRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActionLogService logService;

    @InjectMocks
    private KycRequestService kycRequestService;

    @Test
    public void testCreateRequestForJastiper() {
        User user = new User();
        user.setUsername("a");
        user.setRole("ROLE_TITIPER");
        user.setStatus("ACTIVE");

        KycRequest result = kycRequestService.createRequestForJastiper(user, "test@gmail.com", "a a ron", "a@gmail.com");

        assertEquals(user, result.getUser());
        assertEquals(Role.ROLE_JASTIPER, result.getRequestedRole());
        assertEquals(Status.ACTIVE, result.getStatus());
        assertEquals("test@gmail.com", result.getEmail());
        assertEquals("a a ron", result.getFullName());
        assertEquals("a@gmail.com", result.getSocials());
        assertEquals(Status.PENDING.toString(), user.getStatus());

        verify(kycRequestRepository).save(result);
        verify(userRepository).save(user);
        verify(logService).log(
                eq("Submitted an application"),
                eq("a"),
                eq("ROLE_TITIPER"),
                eq(null),
                contains("submitted an application"),
                eq(LogType.INFO)
        );
    }

    @Test
    public void testGetRequestCountByStatus() {
        List<Object[]> rows = List.of(
                new Object[]{"ACTIVE", 2L},
                new Object[]{"INACTIVE", 1L}
        );

        when(kycRequestRepository.countRequestByStatus()).thenReturn(rows);

        Map<String, Long> result = kycRequestService.getRequestCountByStatus();

        assertEquals(2L, result.get("ACTIVE"));
        assertEquals(1L, result.get("INACTIVE"));

        verify(kycRequestRepository).countRequestByStatus();
    }

    @Test
    void testGetRequestList() {
        List<KycRequest> requests = List.of(
                new KycRequest(),
                new KycRequest()
        );

        when(kycRequestRepository.getPendingRequests()).thenReturn(requests);

        List<KycRequest> result = kycRequestService.getRequestList();

        assertEquals(2, result.size());
        verify(kycRequestRepository).getPendingRequests();
    }

    @Test
    void testCloseAcceptedRequestSuccess() {
        UUID requestId = UUID.randomUUID();

        User admin = new User();
        admin.setUsername("admin");
        admin.setRole("ROLE_ADMIN");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("user");
        user.setRole("ROLE_TITIPER");
        user.setStatus("PENDING");

        KycRequest request = new KycRequest();
        request.setUser(user);
        request.setStatus(Status.ACTIVE);

        when(kycRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        kycRequestService.closeAcceptedRequest(admin, requestId);

        assertEquals(Role.ROLE_JASTIPER.toString(), user.getRole());
        assertEquals(Status.ACTIVE.toString(), user.getStatus());
        assertEquals(Status.INACTIVE, request.getStatus());

        verify(kycRequestRepository).save(request);
        verify(userRepository).save(user);
        verify(logService).log(
                eq("Accept kyc application"),
                eq("admin"),
                eq("ROLE_ADMIN"),
                eq("user"),
                contains("accepted"),
                eq(LogType.INFO)
        );
    }

    @Test
    void testCloseAcceptedRequestUnauthorized() {
        UUID requestId = UUID.randomUUID();

        User fakeAdmin = new User();
        fakeAdmin.setUsername("user");
        fakeAdmin.setRole("ROLE_TITIPER");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setRole("ROLE_TITIPER");

        KycRequest request = new KycRequest();
        request.setUser(user);

        when(kycRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        kycRequestService.closeAcceptedRequest(fakeAdmin, requestId);

        verify(logService).log(
                eq("Unauthorized action"),
                eq("user"),
                eq("ROLE_TITIPER"),
                eq(null),
                contains("unauthorized"),
                eq(LogType.DANGER)
        );

        verify(kycRequestRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testCloseAcceptedRequestInactive() {
        UUID requestId = UUID.randomUUID();

        User admin = new User();
        admin.setRole("ROLE_ADMIN");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setRole("ROLE_TITIPER");
        user.setStatus("PENDING");

        KycRequest request = new KycRequest();
        request.setUser(user);
        request.setStatus(Status.INACTIVE);

        when(kycRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThrows(
                RuntimeException.class,
                () -> kycRequestService.closeAcceptedRequest(admin, requestId)
        );
    }

    @Test
    void testCloseAcceptedRequestInvalidStatus() {
        UUID requestId = UUID.randomUUID();

        User admin = new User();
        admin.setRole("ROLE_ADMIN");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setRole("ROLE_TITIPER");
        user.setStatus(Status.ACTIVE.toString());

        KycRequest request = new KycRequest();
        request.setUser(user);
        request.setStatus(Status.ACTIVE);

        when(kycRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThrows(
                RuntimeException.class,
                () -> kycRequestService.closeAcceptedRequest(admin, requestId)
        );
    }

    @Test
    void testCloseRejectedRequestSuccess() {
        UUID requestId = UUID.randomUUID();

        User admin = new User();
        admin.setUsername("admin");
        admin.setRole("ROLE_ADMIN");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("user");
        user.setRole("ROLE_TITIPER");
        user.setStatus("PENDING");

        KycRequest request = new KycRequest();
        request.setUser(user);
        request.setStatus(Status.ACTIVE);

        when(kycRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        kycRequestService.closeRejectedRequest(admin, requestId);

        assertEquals(Role.ROLE_TITIPER.toString(), user.getRole());
        assertEquals(Status.ACTIVE.toString(), user.getStatus());
        assertEquals(Status.INACTIVE, request.getStatus());

        verify(kycRequestRepository).save(request);
        verify(userRepository).save(user);
        verify(logService).log(
                eq("Rejected kyc application"),
                eq("admin"),
                eq("ROLE_ADMIN"),
                eq("user"),
                contains("rejected"),
                eq(LogType.WARN)
        );
    }

    @Test
    void testCloseRejectedRequestUnauthorized() {
        UUID requestId = UUID.randomUUID();

        User fakeAdmin = new User();
        fakeAdmin.setUsername("user");
        fakeAdmin.setRole("ROLE_TITIPER");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setRole("ROLE_TITIPER");

        KycRequest request = new KycRequest();
        request.setUser(user);

        when(kycRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        kycRequestService.closeRejectedRequest(fakeAdmin, requestId);

        verify(logService).log(
                eq("Unauthorized action"),
                eq("user"),
                eq("ROLE_TITIPER"),
                eq(null),
                contains("unauthorized"),
                eq(LogType.DANGER)
        );

        verify(kycRequestRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testCloseRejectedRequestInactive() {
        UUID requestId = UUID.randomUUID();

        User admin = new User();
        admin.setRole("ROLE_ADMIN");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setRole("ROLE_TITIPER");
        user.setStatus("PENDING");

        KycRequest request = new KycRequest();
        request.setUser(user);
        request.setStatus(Status.INACTIVE);

        when(kycRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThrows(
                RuntimeException.class,
                () -> kycRequestService.closeRejectedRequest(admin, requestId)
        );
    }

    @Test
    void testCloseRejectedRequestInvalidStatus() {
        UUID requestId = UUID.randomUUID();

        User admin = new User();
        admin.setRole("ROLE_ADMIN");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setRole("ROLE_TITIPER");
        user.setStatus(Status.ACTIVE.toString());

        KycRequest request = new KycRequest();
        request.setUser(user);
        request.setStatus(Status.ACTIVE);

        when(kycRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThrows(
                RuntimeException.class,
                () -> kycRequestService.closeRejectedRequest(admin, requestId)
        );
    }

    @Test
    void testGetByIdSuccess() {
        UUID id = UUID.randomUUID();
        KycRequest request = new KycRequest();
        when(kycRequestRepository.findById(id)).thenReturn(Optional.of(request));

        KycRequest result = kycRequestService.getById(id);
        assertEquals(request, result);
    }

    @Test
    void testGetByIdFail() {
        UUID id = UUID.randomUUID();
        when(kycRequestRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> kycRequestService.getById(id)
        );
    }
}
