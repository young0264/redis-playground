package org.example.week2.PER;

public class RedisPERData<T> {

    private T cachedData; private int expiryGapMs;

    public RedisPERData(T cachedData, int expiryGapMs) {
        this.cachedData = cachedData;
        this.expiryGapMs = expiryGapMs;
    }

    public T getCachedData() {
        return cachedData;
    }

    public int getExpiryGapMs() {
        return expiryGapMs;
    }

}
