package com.femcoders.tico.service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

@Service
public class RateLimiterService {

  private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

  public boolean tryConsume(String key) {
    return buckets.computeIfAbsent(key, this::newBucket).tryConsume(1);
  }

  private Bucket newBucket(String key) {
    return Bucket.builder()
        .addLimit(Bandwidth.builder()
            .capacity(3)
            .refillIntervally(3, Duration.ofMinutes(15))
            .build())
        .build();
  }
}
