package org.example.week2.LRU;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LRU(Least Recently Used) 캐시 메모리 관리 정책>
 *   - 가장 오랫동안 사용되지 않은 항목을 제거하는 전략
 **/
public class LRUCacheWithLinkedHashMap<K, V> extends LinkedHashMap<K, V> {

    private final int maxSize;

    public LRUCacheWithLinkedHashMap(int maxSize) {
        // accessOrder=true → 최근 사용 순으로 정렬됨
        super(maxSize, 0.75f, true);
        this.maxSize = maxSize;
    }

    // 가장 오래된 항목 제거 기준
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }

}
