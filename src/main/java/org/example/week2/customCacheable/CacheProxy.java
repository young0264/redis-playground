package org.example.week2.customCacheable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class CacheProxy implements InvocationHandler {
    private final Object target;
    private final RedisCacheManager cacheManager;

    public CacheProxy(Object target, RedisCacheManager cacheManager) {
        this.target = target;
        this.cacheManager = cacheManager;
    }

    public static Object createProxy(Object target, RedisCacheManager cacheManager) {
        return Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                new CacheProxy(target, cacheManager)
        );
    }

    // 실제 메서드가 호출되기 전에 이 메서드가 먼저 실행됨.
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MyCacheable annotation = method.getAnnotation(MyCacheable.class);
        if (annotation == null) {
            return method.invoke(target, args);
        }
        String cacheableKey = annotation.key() + ":" + (args != null ? args[0] : "null");
        Object cachedValue = cacheManager.get(cacheableKey);

        // 캐시에 값이 있으면 캐시 데이터를 뱉고 return
        if (cachedValue != null) {
            System.out.println("캐시 HIT: " + cacheableKey);
            return cachedValue;
        }

        // 캐시에 값이 없으면 메서드 실행
        Object result = method.invoke(target, args);
        cacheManager.put(cacheableKey, result, annotation.ttl());
        System.out.println("캐시 MISS → 저장: " + cacheableKey);
        return result;
    }

}
