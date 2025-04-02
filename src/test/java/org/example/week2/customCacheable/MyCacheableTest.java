package org.example.week2.customCacheable;

import org.example.redis.week2.Product;
import org.example.redis.week2.customCacheable.CacheProxy;
import org.example.redis.week2.customCacheable.MyCacheable;
import org.example.redis.week2.customCacheable.ProductService;
import org.example.redis.week2.customCacheable.RedisCacheManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import static org.junit.jupiter.api.Assertions.*;

class MyCacheableTest {

    private RedissonClient redisson;
    private RedisCacheManager cacheManager;
    private ProductService cachedService; // 인터페이스.
    private SpyProductService originalService;

    @BeforeEach
    void setUp() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        redisson = Redisson.create(config);
        redisson.getKeys().flushall(); // 테스트 전에 캐시 비우기

        cacheManager = new RedisCacheManager(redisson);
        originalService = new SpyProductService(); // 호출 여부 확인용
        cachedService = (ProductService) CacheProxy.createProxy(originalService, cacheManager);
    }

    @Test
    @DisplayName("캐시가 비어있을 때는 실제 메서드가 실행된다")
    void testCacheMiss() {
        Product product = cachedService.getProduct("item:1");
        assertEquals("샴푸", product.name());
        assertEquals(1, originalService.getInvocationCount());
    }

    @Test
    @DisplayName("캐시에 값이 있으면 메서드를 호출하지 않고 캐시에서 반환된다")
    void testCacheHit() {
        Product first = cachedService.getProduct("item:1"); // MISS
        Product second = cachedService.getProduct("item:1"); // HIT

        assertEquals("샴푸", second.name());
        assertEquals(1, originalService.getInvocationCount()); // 실제 호출은 1번만
    }

    // 테스트를 위한 ProductService 구현
    static class SpyProductService implements ProductService {
        private int invocationCount = 0;

        @Override
        @MyCacheable(key = "product", ttl = 60)
        public Product getProduct(String id) {
            invocationCount++;
            System.out.println("=== 실제 메서드 실행 ===");
            return new Product(id, "샴푸", 3000);
        }

        public int getInvocationCount() {
            return invocationCount;
        }
    }

}