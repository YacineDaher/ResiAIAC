package org.aminesidki.resiaiac.configuration;

import java.time.Duration;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        // GenericJacksonJsonRedisSerializer does NOT embed type ("@class") info by
        // default. Without it, on a cache read Jackson has nothing to tell it which
        // class to reconstruct, so it falls back to a generic LinkedHashMap instead
        // of the real DTO (e.g. UtilisateurDto) -> ClassCastException at the call site.
        // Fix: explicitly enable default typing, restricted to our own DTO package
        // so we don't allow arbitrary types to be deserialized (security best practice).
        PolymorphicTypeValidator typeValidator =
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType("org.aminesidki.resiaiac.dto.")
                        .build();

        RedisCacheConfiguration config =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(3))
                        .disableCachingNullValues()
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair.fromSerializer(
                                        GenericJacksonJsonRedisSerializer.builder()
                                                .enableDefaultTyping(typeValidator)
                                                .build()));

        return builder -> builder.cacheDefaults(config);
    }
}