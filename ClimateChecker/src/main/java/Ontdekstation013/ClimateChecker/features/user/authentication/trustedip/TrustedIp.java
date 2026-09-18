package Ontdekstation013.ClimateChecker.features.user.authentication.trustedip;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "trusted_ip")
@Getter
@Setter
@NoArgsConstructor
public class TrustedIp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "ip_hash", nullable = false)
    private String ipHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public TrustedIp(Long userId, String ipHash, LocalDateTime createdAt) {
        this.userId = userId;
        this.ipHash = ipHash;
        this.createdAt = createdAt;
    }
}
