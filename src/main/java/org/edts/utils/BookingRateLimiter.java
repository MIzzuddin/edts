package org.edts.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class BookingRateLimiter {
    private static final String KEY_PREFIX = "concert_booking:";
    private static final int MAX_BOOKINGS_PER_SECOND = 100;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public boolean allowBooking(Long concertId, int quantity) {
        String key = KEY_PREFIX + concertId + ":" + Instant.now().getEpochSecond();
        Long currentCount = redisTemplate.opsForValue().increment(key, quantity);

        if (currentCount == quantity) {
            redisTemplate.expire(key, Duration.ofSeconds(2));
        }

        return currentCount <= MAX_BOOKINGS_PER_SECOND;
    }
}

