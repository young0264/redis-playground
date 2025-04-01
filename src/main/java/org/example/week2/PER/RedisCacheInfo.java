package org.example.week2.PER;

public class RedisCacheInfo {

    private final String key;
    private final int cacheKeepSecond;

    public RedisCacheInfo(String key, int cacheKeepSecond) {
        this.key = key;
        this.cacheKeepSecond = cacheKeepSecond;
    }

    public String getKey() {
        return key;
    }

    public int getCacheKeepSecond() {
        return cacheKeepSecond;
    }

}
