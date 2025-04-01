package org.example.week2.PER;

import org.example.week2.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductService {
    List<Product> products = new ArrayList<>();

    // 실제로는 DB 조회 로직이 들어가야 합니다.
    public ProductService() {
        products.add(new Product("1", "맥북", 2000000));
        products.add(new Product("2", "모니터", 300000));
        products.add(new Product("3", "키보드", 50000));
    }

    public List<Product> getList() {
        // 여기서는 예시로 하드코딩된 리스트를 반환합니다.
        return this.products;
    }

}
