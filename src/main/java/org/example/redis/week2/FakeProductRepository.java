package org.example.redis.week2;

import java.util.HashMap;
import java.util.Map;

public class FakeProductRepository {

    private final Map<String, Product> db = new HashMap<>();
    private int findCallCount = 0;

    // 기본 생성자로 key-value 가짜 데이터 insert(put)
    public FakeProductRepository() {
        db.put("1", new Product("1", "샴푸", 5000));
        db.put("2", new Product("2", "비누", 3000));
        db.put("3", new Product("3", "칫솔", 2000));
    }

    public Product findById(String id) {
        System.out.println("DB Access: " + id);
        Product product = db.get(id);
        if (product != null) {
            findCallCount++;
        }
        return product;
    }

    public void update(String id, Product product) {
        db.put(id, product);
    }

    public int getFindCallCount() {
        return findCallCount;
    }

    public void resetFindCallCount() {
        findCallCount = 0;
    }

}
