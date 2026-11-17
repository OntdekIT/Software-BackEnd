package Ontdekstation013.ClimateChecker.snapshot;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Zet X-Data-As-Of (ISO UTC, moment van de laatste geslaagde ingest) op de kaartgerelateerde antwoorden. */
@Component
@RequiredArgsConstructor
public class DataFreshnessFilter extends OncePerRequestFilter {
    public static final String HEADER = "X-Data-As-Of";

    private final SnapshotService snapshotService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().toLowerCase();
        return !(path.startsWith("/api/neighbourhood/") || path.startsWith("/api/meetstation/")
                || path.startsWith("/api/measurement/") || path.equals("/api/snapshot") || path.equals("/api/status"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Instant dataAsOf = snapshotService.dataAsOf();
        if (dataAsOf != null) {
            response.setHeader(HEADER, dataAsOf.toString());
        }
        chain.doFilter(request, response);
    }
}
