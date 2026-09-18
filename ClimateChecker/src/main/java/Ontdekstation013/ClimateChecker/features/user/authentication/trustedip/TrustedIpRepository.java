package Ontdekstation013.ClimateChecker.features.user.authentication.trustedip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrustedIpRepository extends JpaRepository<TrustedIp, Long> {
    boolean existsByUserIdAndIpHash(Long userId, String ipHash);
}
