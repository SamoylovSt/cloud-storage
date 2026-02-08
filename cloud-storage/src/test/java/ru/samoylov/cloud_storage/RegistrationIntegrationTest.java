package ru.samoylov.cloud_storage;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.samoylov.cloud_storage.dto.RegisterRequestDTO;
import ru.samoylov.cloud_storage.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public class RegistrationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UserService userService;


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void cleanDb() {
        jdbcTemplate.execute("TRUNCATE TABLE users");
    }

    @Test
    void shouldIncreaseUserCountForSave() {
        Long initialCount = jdbcTemplate.queryForObject("SELECT  COUNT(*) FROM users", Long.class);
        userService.saveUser(new RegisterRequestDTO("111111", "111111"));
        Long newCount = jdbcTemplate.queryForObject("SELECT  COUNT(*) FROM users", Long.class);
        assertEquals(initialCount + 1, newCount);
    }

    @Test
    void shouldReturn409WhereUserNotUnique() {
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO("111111", "111111");
        userService.saveUser(registerRequestDTO);
        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/sign-up",
                registerRequestDTO,
                String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void shouldReturn401WhenUserNotRegistered() {
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO("111111", "111111");
        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/sign-in",
                registerRequestDTO,
                String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

}
