package id.ac.ui.cs.advprog.groupproject.order.adapter;

import id.ac.ui.cs.advprog.groupproject.order.port.JastiperMetricsPort;
import id.ac.ui.cs.advprog.groupproject.auth.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserMetricsAdapter implements JastiperMetricsPort {

    private final UserRepository userRepository;

    public UserMetricsAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void incrementTriedToSell(UUID jastiperId) {
        try {
            userRepository.findById(jastiperId).ifPresent(jastiper -> {
                jastiper.setTriedToSell(jastiper.getTriedToSell() + 1);
                userRepository.save(jastiper);
            });
        } catch (Exception e) {
            // Log but don't fail the checkout if success rate update fails
        }
    }

    @Override
    public void incrementSuccessfullySold(UUID jastiperId) {
        try {
            userRepository.findById(jastiperId).ifPresent(jastiper -> {
                jastiper.setSuccessfullySold(jastiper.getSuccessfullySold() + 1);
                userRepository.save(jastiper);
            });
        } catch (Exception e) {
            // Log but don't fail the status update if success rate update fails
        }
    }
}
