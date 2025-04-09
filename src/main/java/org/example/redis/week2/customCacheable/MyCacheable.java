package org.example.redis.week2.customCacheable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * about @Cacheable:
 *  ...
 *  target method will be invoked and the returned value will be stored in the associated cache.
 *  ...
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyCacheable {

    String key();           // 캐시 키 prefix
    long ttl() default 60;  // TTL (초 단위)
}
