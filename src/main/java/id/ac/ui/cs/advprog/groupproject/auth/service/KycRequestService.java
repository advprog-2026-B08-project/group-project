package id.ac.ui.cs.advprog.groupproject.auth.service;

import id.ac.ui.cs.advprog.groupproject.auth.model.*;
import id.ac.ui.cs.advprog.groupproject.auth.repository.KycRequestRepository;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class KycRequestService {
    private final KycRequestRepository kycRequestRepository;
    private final UserRepository userRepository;
    private final ActionLogService logService;

    public KycRequestService(KycRequestRepository kycRequestRepository,
                             UserRepository userRepository,
                             ActionLogService logService) {
        this.kycRequestRepository = kycRequestRepository;
        this.userRepository = userRepository;
        this.logService = logService;
    }
    public KycRequest createRequestForJastiper(User user, String email,
                                               String fullName, String socials) {
        KycRequest request = new KycRequest();
        request.setUser(user);
        request.setRequestedRole(Role.ROLE_JASTIPER);
        request.setStatus(Status.ACTIVE);

        request.setEmail(email);
        request.setFullName(fullName);
        request.setSocials(socials);

        user.setStatus(Status.PENDING.toString());

        kycRequestRepository.save(request);
        userRepository.save(user);

        String description = user.getUsername()
                + " submitted an application to be a jastiper";
        logService.log("Submitted an application", user.getUsername(),
                user.getRole(), null, description, LogType.INFO);

        return request;
    }

    public Map<String, Long> getRequestCountByStatus() {
        List<Object[]> result = kycRequestRepository.countRequestByStatus();
        Map<String, Long> map = new HashMap<>();

        for (Object[] row: result) {
            String status = row[0].toString();
            Long count = (Long) row[1];
            map.put(status, count);
        }

        return map;
    }

    public List<KycRequest> getRequestList() {
        return kycRequestRepository.getPendingRequests();
    }

    public void closeAcceptedRequest(User admin, UUID requestId) {
        KycRequest request = kycRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found!"));
        User user = userRepository.findById(request.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not Found!"));

        if (!admin.getRole().equals("ROLE_ADMIN")) {
            String description = admin.getUsername()
                    + "tried to perform an unauthorized action of accepting a kyc request";
            logService.log("Unauthorized action", admin.getUsername(),
                    admin.getRole(), null, description, LogType.DANGER);
            return;
        }
        if (!request.getUser().getRole().equals("ROLE_TITIPER")) {
            throw new RuntimeException("Invalid user role!");
        }
        if (!request.getStatus().toString().equals("ACTIVE")) {
            throw new RuntimeException("Invalid requestStatus");
        }

        if (!user.getRole().equals(Role.ROLE_TITIPER.toString())) {
            throw new RuntimeException("Invalid role!");
        }
        if (!user.getStatus().equals(Status.PENDING.toString())) {
            throw new RuntimeException("Invalid current status!");
        }

        request.setStatus(Status.INACTIVE);
        kycRequestRepository.save(request);

        user.setStatus(Status.ACTIVE.toString());
        user.setRole(Role.ROLE_JASTIPER.toString());
        user.setSuccessfully_sold(0);
        user.setTried_to_sell(0);
        userRepository.save(user);

        String description = admin.getUsername()
                + " accepted "
                + user.getUsername()
                + "'s application to be a jastiper";

        logService.log("Accept kyc application", admin.getUsername(),
                admin.getRole(), user.getUsername(), description, LogType.INFO);
    }

    public void closeRejectedRequest(User admin, UUID requestId) {
        KycRequest request = kycRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found!"));
        User user = userRepository.findById(request.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not Found!"));

        if(!admin.getRole().equals("ROLE_ADMIN")) {
            String description = admin.getUsername()
                    + "tried to perform an unauthorized action of rejecting a kyc request";
            logService.log("Unauthorized action", admin.getUsername(),
                    admin.getRole(), null, description, LogType.DANGER);
            return;
        }
        if (!request.getUser().getRole().equals("ROLE_TITIPER")) {
            throw new RuntimeException("Invalid user role!");
        }
        if (!request.getStatus().toString().equals("ACTIVE")) {
            throw new RuntimeException("Invalid requestStatus");
        }

        if (!user.getRole().equals(Role.ROLE_TITIPER.toString())) {
            throw new RuntimeException("Invalid role!");
        }
        if (!user.getStatus().equals(Status.PENDING.toString())) {
            throw new RuntimeException("Invalid current status!");
        }

        request.setStatus(Status.INACTIVE);
        kycRequestRepository.save(request);

        user.setStatus(Status.ACTIVE.toString());
        userRepository.save(user);

        String description = admin.getUsername()
                + " rejected "
                + user.getUsername()
                + "'s application to be a jastiper";
        logService.log("Rejected kyc application", admin.getUsername(),
                admin.getRole(), user.getUsername(), description, LogType.WARN);
    }

    public KycRequest getById(UUID id) {
        return kycRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found!"));
    }
}
