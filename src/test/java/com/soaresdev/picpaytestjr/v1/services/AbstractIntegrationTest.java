package com.soaresdev.picpaytestjr.v1.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@Testcontainers
@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractIntegrationTest {
    protected static final String POSTGRESQL_IMAGE = "postgres:17.4";
    protected static final String REDIS_IMAGE = "redis:7.4.2";

    @LocalServerPort
    protected int port;

    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        cleanAllCaches();
    }

    @Container
    protected static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>(POSTGRESQL_IMAGE)
            .withDatabaseName("testing")
            .withUsername("testing_user")
            .withPassword("testing_password");

    @Container
    protected static GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>(REDIS_IMAGE).
            withExposedPorts(6379);

    @RegisterExtension
    protected static WireMockExtension WIREMOCK_SERVER = WireMockExtension.newInstance().
            options(wireMockConfig().dynamicPort()).build();

    @DynamicPropertySource
    protected static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRE_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRE_SQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRE_SQL_CONTAINER::getPassword);
        registry.add("external.api.base-url", WIREMOCK_SERVER::baseUrl);
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));
    }

    protected void cleanAllCaches() {
        for (String name : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(name);
            if (Objects.nonNull(cache))
                cache.clear();
        }
    }
}