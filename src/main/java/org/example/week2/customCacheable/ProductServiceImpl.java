package org.example.week2.customCacheable;

import org.example.week2.Product;

/**
 * ProductService가 class 라면 → Java Proxy (java.lang.reflect.Proxy) 기반 프록시로는 처리할 수 없음
 **/
public class ProductServiceImpl implements ProductService {

    @Override
    public Product getProduct(String id) {
        System.out.println("DB에서 상품 조회: " + id);
        return new Product(id, "샴푸", 3000);
    }

}
