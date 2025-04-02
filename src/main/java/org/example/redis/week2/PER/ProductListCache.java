package org.example.redis.week2.PER;

import org.example.redis.week2.Product;
import org.redisson.api.RedissonClient;

import java.util.List;

public class ProductListCache extends RedisPERCache<List<Product>> {
    private static final int PRODUCT_LIST_CACHE_TTL = 60;

    public ProductListCache(RedissonClient redisson) {
        super(redisson);
    }

    @Override
    protected RedisCacheInfo getCacheInfo() {
        return new RedisCacheInfo("product:list", PRODUCT_LIST_CACHE_TTL);
    }

    @Override
    protected List<Product> getResourceData() {
        ProductService productService = new ProductService();
        return productService.getList(); // 실제 서비스 로직
    }

    @Override
    protected String getCacheKey() {
        return getCacheInfo().getKey();
    }
}
