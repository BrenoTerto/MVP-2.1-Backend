package com.redepatas.api.services;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

  private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

  public Bucket resolveBucket(String key) {
    return cache.computeIfAbsent(key, k -> {
      Refill refill = Refill.intervally(1, Duration.ofSeconds(30)); // 1 tentativa a cada 30s
      Bandwidth limit = Bandwidth.classic(1, refill);
      return Bucket.builder().addLimit(limit).build();
    });
  }
}
