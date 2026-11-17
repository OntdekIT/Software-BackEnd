package Ontdekstation013.ClimateChecker.utility;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class ClientIpResolverTest {

    private final ClientIpResolver resolver = new ClientIpResolver("10.0.0.0/8, 192.168.1.5, ::1");

    private MockHttpServletRequest request(String remoteAddr, String forwardedFor) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(remoteAddr);
        if (forwardedFor != null) {
            request.addHeader("X-Forwarded-For", forwardedFor);
        }
        return request;
    }

    @Test
    void usesRemoteAddressWhenThereIsNoHeader() {
        assertThat(resolver.resolve(request("203.0.113.9", null))).isEqualTo("203.0.113.9");
    }

    @Test
    void usesClientFromHeaderWhenRequestComesFromTrustedProxy() {
        assertThat(resolver.resolve(request("10.1.2.3", "203.0.113.42"))).isEqualTo("203.0.113.42");
    }

    @Test
    void ignoresHeaderFromUntrustedPeer() {
        assertThat(resolver.resolve(request("198.51.100.7", "203.0.113.42"))).isEqualTo("198.51.100.7");
    }

    @Test
    void ignoresClientSuppliedEntriesLeftOfTheProxy() {
        // De client stuurde "1.1.1.1"; de proxy voegde het echte adres toe.
        assertThat(resolver.resolve(request("10.0.0.1", "1.1.1.1, 203.0.113.42"))).isEqualTo("203.0.113.42");
    }

    @Test
    void skipsChainedTrustedProxies() {
        assertThat(resolver.resolve(request("10.0.0.1", "203.0.113.42, 10.0.0.2, 192.168.1.5")))
                .isEqualTo("203.0.113.42");
    }

    @Test
    void supportsSingleAddressAndIpv6Entries() {
        assertThat(resolver.resolve(request("192.168.1.5", "203.0.113.42"))).isEqualTo("203.0.113.42");
        assertThat(resolver.resolve(request("::1", "203.0.113.42"))).isEqualTo("203.0.113.42");
    }

    @Test
    void neverTreatsAnUnparseableHopAsATrustedProxy() {
        assertThat(resolver.resolve(request("10.0.0.1", "not-an-ip"))).isEqualTo("not-an-ip");
    }

    @Test
    void trustsNoProxyWhenNoneAreConfigured() {
        ClientIpResolver none = new ClientIpResolver("");
        assertThat(none.resolve(request("10.0.0.1", "203.0.113.42"))).isEqualTo("10.0.0.1");
    }
}
