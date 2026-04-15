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
    public KycRequest createRequestForJastiper(User user) {
        KycRequest request = new KycRequest();
        request.setUser(user);
        request.setRequestedRole(Role.ROLE_JASTIPER);
        request.setStatus(Status.Pending);

        kycRequestRepository.save(request);
        return request;
    }

    public KycRequest createRequestForAdmin(User user) {
        KycRequest request = new KycRequest();
        request.setUser(user);
        request.setRequestedRole(Role.ROLE_JASTIPER);
        request.setStatus(Status.Pending);

        kycRequestRepository.save(request);
        return request;
    }
}
