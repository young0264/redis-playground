package org.example.redis.week2.customCacheable;

import org.example.redis.week2.Product;

public interface ProductService {
    @MyCacheable(key = "item:shampoo", ttl = 3)
    Product getProduct(String id);
}
