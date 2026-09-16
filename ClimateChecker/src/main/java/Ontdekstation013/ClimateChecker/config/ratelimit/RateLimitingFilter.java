package Ontdekstation013.ClimateChecker.config.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * In-memory rate limiting filter based on Bucket4J.
 * <p>
 * Limits each client to a configurable number of requests per minute (default 20).
 * The client is identified by the first IP in the {@code X-Forwarded-For} header
 * when present, otherwise by the request remote address.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitingFilter extends OncePerRequestFilter {

    private final boolean enabled;
    private final int requestsPerMinute;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitingFilter(
            @Value("${rate.limit.enabled:true}") boolean enabled,
            @Value("${rate.limit.requests-per-minute:20}") int requestsPerMinute) {
        this.enabled = enabled;
        this.requestsPerMinute = requestsPerMinute;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = getClientKey(request);
        Bucket bucket = buckets.computeIfAbsent(clientKey, key -> createBucket());

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        response.setHeader("X-Rate-Limit-Limit", String.valueOf(requestsPerMinute));

        if (probe.isConsumed()) {
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            long waitMillis = probe.getNanosToWaitForRefill() / 1_000_000;

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("X-Rate-Limit-Remaining", "0");
            response.setHeader("X-Rate-Limit-Retry-After-Millis", String.valueOf(waitMillis));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(buildErrorResponse(waitMillis));
        }
    }

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(
                requestsPerMinute,
                Refill.intervally(requestsPerMinute, Duration.ofMinutes(1)));

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private String getClientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String buildErrorResponse(long retryAfterMillis) {
        String timestamp = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
        return String.format(
                "{\"timestamp\":\"%s\",\"error\":\"TOO_MANY_REQUESTS\",\"statusCode\":429,"
                        + "\"message\":\"Rate limit exceeded. Try again in %d milliseconds.\"}",
                timestamp, retryAfterMillis);
    }
}
