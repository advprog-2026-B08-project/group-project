package id.ac.ui.cs.advprog.groupproject.auth.service;

import id.ac.ui.cs.advprog.groupproject.auth.model.KycRequest;
import id.ac.ui.cs.advprog.groupproject.auth.model.Role;
import id.ac.ui.cs.advprog.groupproject.auth.model.Status;
import id.ac.ui.cs.advprog.groupproject.auth.model.User;
import id.ac.ui.cs.advprog.groupproject.auth.repository.KycRequestRepository;
import org.springframework.stereotype.Service;

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
        request.setStatus(Status.Pending);

        request.setEmail(email);
        request.setFullName(fullName);
        request.setSocials(socials);

        kycRequestRepository.save(request);
        return request;
    }

    public KycRequest createRequestForAdmin(User user, String email, String fullName,
                                            String phoneNumber, String socials) {
        KycRequest request = new KycRequest();
        request.setUser(user);
        request.setRequestedRole(Role.ROLE_JASTIPER);
        request.setStatus(Status.Pending);

        request.setEmail(email);
        request.setFullName(fullName);
        request.setPhoneNumber(phoneNumber);
        request.setSocials(socials);

        kycRequestRepository.save(request);
        return request;
    }
}
