package Ontdekstation013.ClimateChecker.utility;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Component;

/**
 * Bepaalt het echte client-IP, ook achter een reverse proxy.
 * <p>
 * {@code X-Forwarded-For} wordt alleen geloofd als de directe verbinding van een
 * bekende proxy komt ({@code application.trusted-proxies}, IP's of CIDR-reeksen).
 * De header wordt van rechts naar links doorlopen: elke proxy voegt het adres van
 * zijn afzender rechts toe, dus alles links van de laatste vertrouwde proxy is
 * door de client aan te leveren en dus te vervalsen. Het eerste niet-vertrouwde
 * adres van rechts is het echte client-IP.
 */
@Component
public class ClientIpResolver {

    static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";

    private final List<IpAddressMatcher> trustedProxies;

    public ClientIpResolver(
            @Value("${application.trusted-proxies:127.0.0.0/8,::1,10.0.0.0/8,172.16.0.0/12,192.168.0.0/16}")
            String trustedProxies) {
        this.trustedProxies = Arrays.stream(trustedProxies.split(","))
                .map(String::trim)
                .filter(entry -> !entry.isEmpty())
                .map(IpAddressMatcher::new)
                .toList();
    }

    public String resolve(HttpServletRequest request) {
        String remoteAddress = request.getRemoteAddr();
        String forwardedFor = request.getHeader(FORWARDED_FOR_HEADER);
        if (forwardedFor == null || forwardedFor.isBlank() || !isTrustedProxy(remoteAddress)) {
            return remoteAddress;
        }

        String[] hops = forwardedFor.split(",");
        for (int i = hops.length - 1; i >= 0; i--) {
            String hop = hops[i].trim();
            if (hop.isEmpty()) {
                continue;
            }
            if (!isTrustedProxy(hop)) {
                return hop;
            }
        }
        // Alle hops zijn proxies: de oorspronkelijke client staat links.
        return hops[0].trim().isEmpty() ? remoteAddress : hops[0].trim();
    }

    private boolean isTrustedProxy(String address) {
        if (address == null || address.isBlank()) {
            return false;
        }
        try {
            return trustedProxies.stream().anyMatch(matcher -> matcher.matches(address));
        } catch (IllegalArgumentException e) {
            // Geen geldig IP-adres (bijv. een hostnaam in de header): nooit vertrouwen.
            return false;
        }
    }
}
