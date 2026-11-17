package Ontdekstation013.ClimateChecker.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Caffeine-caches voor de kaartendpoints. Elke cache heeft een TTL en een
 * maximumgrootte, zodat een client die steeds een andere minuut of een andere
 * time-lapse vraagt het geheugen niet kan laten groeien.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /** Eigen bean zodat tests de tijd kunnen versnellen om verlopen te testen. */
    @Bean
    public Ticker cacheTicker() {
        return Ticker.systemTicker();
    }

    @Bean
    public CacheManager cacheManager(
            Ticker cacheTicker,
            @Value("${map.cache.ttl-seconds:60}") long ttlSeconds,
            @Value("${map.cache.max-entries:120}") long maxEntries,
            @Value("${map.cache.timelapse-max-entries:20}") long timelapseMaxEntries) {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                build(CacheNames.STATIONS_WITH_MEASUREMENTS, ttlSeconds, maxEntries, cacheTicker),
                build(CacheNames.NEIGHBOURHOODS_AT_TIME, ttlSeconds, maxEntries, cacheTicker),
                // Een time-lapse is groot (honderden kB), dus een kleinere grens.
                build(CacheNames.NEIGHBOURHOOD_TIMELAPSE, ttlSeconds, timelapseMaxEntries, cacheTicker)));
        return manager;
    }

    private static CaffeineCache build(String name, long ttlSeconds, long maxEntries, Ticker ticker) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(ttlSeconds))
                .maximumSize(maxEntries)
                .ticker(ticker)
                .recordStats()
                .build());
    }
}
