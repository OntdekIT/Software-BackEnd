package Ontdekstation013.ClimateChecker.features.user.authentication.trustedip;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;

/**
 * Beheert de vertrouwde IP-adressen per gebruiker.
 * <p>
 * Het IP-adres wordt niet in leesbare vorm opgeslagen maar als HMAC-SHA256 met
 * een server-side pepper. Een kale hash van een IP-adres is triviaal terug te
 * rekenen (IPv4 heeft weinig entropie); door te HMAC-en met een geheim dat niet
 * in de database staat, is de opgeslagen waarde nutteloos als de database lekt.
 */
@Service
public class TrustedIpService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final TrustedIpRepository trustedIpRepository;
    private final SecretKeySpec key;

    public TrustedIpService(
            TrustedIpRepository trustedIpRepository,
            @Value("${application.trusted-ip.pepper:dev-only-trusted-ip-pepper-change-me}") String pepper) {
        this.trustedIpRepository = trustedIpRepository;
        this.key = new SecretKeySpec(pepper.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
    }

    /**
     * @return true als het opgegeven IP eerder als vertrouwd is geregistreerd
     * voor deze gebruiker (dan mag de mail-verificatiestap worden overgeslagen).
     */
    public boolean isTrusted(long userId, String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return false;
        }
        return trustedIpRepository.existsByUserIdAndIpHash(userId, hash(ipAddress));
    }

    /**
     * Onthoudt het IP als vertrouwd voor deze gebruiker. Idempotent: een al
     * bekend (user, ip) wordt niet dubbel opgeslagen.
     */
    public void remember(long userId, String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return;
        }
        String ipHash = hash(ipAddress);
        if (!trustedIpRepository.existsByUserIdAndIpHash(userId, ipHash)) {
            trustedIpRepository.save(new TrustedIp(userId, ipHash, LocalDateTime.now()));
        }
    }

    private String hash(String ipAddress) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(key);
            byte[] digest = mac.doFinal(ipAddress.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Kon het IP-adres niet hashen", e);
        }
    }
}
