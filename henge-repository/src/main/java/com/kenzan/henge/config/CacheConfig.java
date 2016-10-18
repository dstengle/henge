package com.kenzan.henge.config;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.cache.CacheBuilder;

@Configuration
@EnableCaching
public class CacheConfig {

    public final static String PROPERTY_GROUP_CACHE = "property_group";
    public final static String VERSION_SET_CACHE = "version_set";
    public final static String VERSION_SET_MAPPING_CACHE = "version_set_mapping";
    public final static String FILE_API_CACHE = "file_api";

    @Value("${cache.expiration.minutes}")
    private int expirationTime;

    @Bean(name = PROPERTY_GROUP_CACHE)
    public Cache propertyGroupCache() {
        return createCache(PROPERTY_GROUP_CACHE);
    }

    @Bean(name = VERSION_SET_CACHE)
    public Cache versionSetCache() {

        return createCache(VERSION_SET_CACHE);
    }

    @Bean(name = VERSION_SET_MAPPING_CACHE)
    public Cache versionSetMappingCache() {

        return createCache(VERSION_SET_MAPPING_CACHE);
    }
    
    @Bean(name = FILE_API_CACHE)
    public Cache fileAPICache() {

        return new GuavaCache(FILE_API_CACHE, CacheBuilder.newBuilder()
            .expireAfterWrite(expirationTime, TimeUnit.MINUTES).build());
    }

    @Bean
    public CacheManager cacheManager() {

        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(propertyGroupCache(), versionSetCache(),
            versionSetMappingCache(), fileAPICache()));
        return cacheManager;
    }

    private Cache createCache(String name) {
        return (expirationTime > 0) ? 
            new GuavaCache(name,
                CacheBuilder.newBuilder()
                .expireAfterWrite(expirationTime, TimeUnit.MINUTES).build()) :
            new GuavaCache(name, 
                CacheBuilder.newBuilder().build());

    }
    
}
