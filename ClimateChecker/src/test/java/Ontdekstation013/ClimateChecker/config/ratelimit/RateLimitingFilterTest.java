package Ontdekstation013.ClimateChecker.config.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitingFilterTest {

    private static final int REQUESTS_PER_MINUTE = 20;

    private final RateLimitingFilter filter = new RateLimitingFilter(true, REQUESTS_PER_MINUTE);

    @Test
    void shouldAllowRequestsWithinLimit() throws ServletException, IOException {
        FilterChain filterChain = mock(FilterChain.class);

        for (int i = 0; i < REQUESTS_PER_MINUTE; i++) {
            MockHttpServletRequest request = createRequest("127.0.0.1");
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilterInternal(request, response, filterChain);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        }
    }

    @Test
    void shouldRejectRequestExceedingLimit() throws ServletException, IOException {
        FilterChain filterChain = mock(FilterChain.class);

        for (int i = 0; i < REQUESTS_PER_MINUTE; i++) {
            MockHttpServletRequest request = createRequest("127.0.0.1");
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilterInternal(request, response, filterChain);
        }

        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();
        filter.doFilterInternal(createRequest("127.0.0.1"), blockedResponse, filterChain);

        assertThat(blockedResponse.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(blockedResponse.getHeader("X-Rate-Limit-Remaining")).isEqualTo("0");
        assertThat(blockedResponse.getHeader("X-Rate-Limit-Limit")).isEqualTo(String.valueOf(REQUESTS_PER_MINUTE));
        assertThat(blockedResponse.getContentAsString()).contains("Rate limit exceeded");
    }

    @Test
    void shouldTrackDifferentClientsSeparately() throws ServletException, IOException {
        FilterChain filterChain = mock(FilterChain.class);

        for (int i = 0; i < REQUESTS_PER_MINUTE; i++) {
            filter.doFilterInternal(createRequest("127.0.0.1"), new MockHttpServletResponse(), filterChain);
        }

        MockHttpServletResponse otherClientResponse = new MockHttpServletResponse();
        filter.doFilterInternal(createRequest("192.168.1.1"), otherClientResponse, filterChain);

        assertThat(otherClientResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(otherClientResponse.getHeader("X-Rate-Limit-Remaining"))
                .isEqualTo(String.valueOf(REQUESTS_PER_MINUTE - 1));
    }

    @Test
    void shouldBypassFilterWhenDisabled() throws ServletException, IOException {
        RateLimitingFilter disabledFilter = new RateLimitingFilter(false, REQUESTS_PER_MINUTE);
        FilterChain filterChain = mock(FilterChain.class);

        for (int i = 0; i < REQUESTS_PER_MINUTE + 5; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            disabledFilter.doFilterInternal(createRequest("127.0.0.1"), response, filterChain);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        }
    }

    @Test
    void shouldUseXForwardedForHeader() throws ServletException, IOException {
        FilterChain filterChain = mock(FilterChain.class);

        MockHttpServletRequest request = createRequest("10.0.0.1");
        request.addHeader("X-Forwarded-For", "203.0.113.42, 70.41.3.18, 150.172.238.178");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    private MockHttpServletRequest createRequest(String remoteAddr) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(remoteAddr);
        return request;
    }
}
