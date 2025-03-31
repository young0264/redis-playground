package org.example.week2;

import org.example.week2.LRU.LRUCacheWithLinkedHashMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LRUCacheWithLinkedHashMapTest {

    @Test
    @DisplayName("LRU 캐시가 가장 오래된 항목을 제거해야 한다")
    void testLruEviction() {

        // size=3, LRU HashMap
        LRUCacheWithLinkedHashMap<Integer, String> cache = new LRUCacheWithLinkedHashMap<>(3);

        cache.put(1, "One");
        cache.put(2, "Two");
        cache.put(3, "Three");

        cache.get(1); // 최근 사용 -> 2
        assertTrue(cache.containsKey(2));

        cache.put(4, "Four"); // 2가 제거됨

        assertFalse(cache.containsKey(2));
        assertTrue(cache.containsKey(1));
        assertTrue(cache.containsKey(3));
        assertTrue(cache.containsKey(4));
    }

}