package id.ac.ui.cs.advprog.groupproject.auth.service;

import id.ac.ui.cs.advprog.groupproject.auth.model.KycRequest;
import id.ac.ui.cs.advprog.groupproject.auth.model.Role;
import id.ac.ui.cs.advprog.groupproject.auth.model.Status;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.repository.KycRequestRepository;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class KycRequestService {
    KycRequestRepository kycRequestRepository;
    UserRepository userRepository;

    public KycRequestService(KycRequestRepository kycRequestRepository,
                             UserRepository userRepository) {
        this.kycRequestRepository = kycRequestRepository;
        this.userRepository = userRepository;
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
        return request;
    }

    public KycRequest createRequestForAdmin(User user, String email, String fullName,
                                            String phoneNumber, String socials) {
        KycRequest request = new KycRequest();
        request.setUser(user);
        request.setRequestedRole(Role.ROLE_JASTIPER);
        request.setStatus(Status.ACTIVE);

        request.setEmail(email);
        request.setFullName(fullName);
        request.setPhoneNumber(phoneNumber);
        request.setSocials(socials);

        user.setStatus(Status.PENDING.toString());

        kycRequestRepository.save(request);
        userRepository.save(user);

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

    public void closeAcceptedRequest(UUID requestId) {
        KycRequest request = kycRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found!"));
        User user = userRepository.findById(request.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not Found!"));

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
        userRepository.save(user);
    }

    public void closeRejectedRequest(UUID requestId) {
        KycRequest request = kycRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found!"));
        User user = userRepository.findById(request.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not Found!"));

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
    }

    public KycRequest getById(UUID id) {
        return kycRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found!"));
    }
}
