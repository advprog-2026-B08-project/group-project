package id.ac.ui.cs.advprog.groupproject.auth.service;

import id.ac.ui.cs.advprog.groupproject.auth.model.KycRequest;
import id.ac.ui.cs.advprog.groupproject.auth.model.Role;
import id.ac.ui.cs.advprog.groupproject.auth.model.Status;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.repository.KycRequestRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KycRequestService {
    KycRequestRepository kycRequestRepository;

    public KycRequestService(KycRequestRepository kycRequestRepository) {
        this.kycRequestRepository = kycRequestRepository;
    }
    public KycRequest createRequestForJastiper(User user, String email,
                                               String fullName, String socials) {
        KycRequest request = new KycRequest();
        request.setUser(user);
        request.setRequestedRole(Role.ROLE_JASTIPER);
        request.setStatus(Status.PENDING);

        request.setEmail(email);
        request.setFullName(fullName);
        request.setSocials(socials);

        kycRequestRepository.save(request);
        user.setStatus(Status.PENDING.toString());
        return request;
    }

    public KycRequest createRequestForAdmin(User user, String email, String fullName,
                                            String phoneNumber, String socials) {
        KycRequest request = new KycRequest();
        request.setUser(user);
        request.setRequestedRole(Role.ROLE_JASTIPER);
        request.setStatus(Status.PENDING);

        request.setEmail(email);
        request.setFullName(fullName);
        request.setPhoneNumber(phoneNumber);
        request.setSocials(socials);

        kycRequestRepository.save(request);
        user.setStatus(Status.PENDING.toString());
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
}
