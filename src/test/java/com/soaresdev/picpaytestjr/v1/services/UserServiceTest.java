package com.soaresdev.picpaytestjr.v1.services;

import com.soaresdev.picpaytestjr.entities.enums.UserType;
import com.soaresdev.picpaytestjr.exceptions.InvalidUserTypeException;
import com.soaresdev.picpaytestjr.repositories.UserRepository;
import com.soaresdev.picpaytestjr.v1.dtos.UserRequestDto;
import com.soaresdev.picpaytestjr.v1.dtos.UserResponseDto;
import jakarta.persistence.EntityExistsException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {
    private static final String POSTGRESQL_IMAGE = "postgres:17.4";
    private static final String VALID_CPF = "47776629911";
    private static final String VALID_CNPJ = "79610519000141";
    private final UserRequestDto userRequestDto;

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    public UserServiceTest() {
        this.userRequestDto = new UserRequestDto();
    }

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        userRequestDto.setBalance(BigDecimal.ONE);
        userRequestDto.setCpfCnpj(VALID_CPF);
        userRequestDto.setEmail("john.doe@gmail.com");
        userRequestDto.setFullName("John Doe");
        userRequestDto.setPassword("the-strongest-password");
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRESQL_IMAGE)
            .withDatabaseName("testing")
            .withUsername("testing_user")
            .withPassword("testing_password");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    @Order(1)
    void shouldCreateCustomer() {
        UserResponseDto userResponseDto = userService.createUser(userRequestDto);

        assertThat(userResponseDto).usingRecursiveComparison().
                ignoringFields("id", "userType").isEqualTo(userRequestDto);
        assertThat(userResponseDto.getId()).isNotNull();
        assertThat(userResponseDto.getUserType()).isEqualTo(UserType.CUSTOMER);
    }

    @Test
    @Order(2)
    void shouldCreateSeller() {
        userRequestDto.setCpfCnpj(VALID_CNPJ);

        UserResponseDto userResponseDto = userService.createUser(userRequestDto);

        assertThat(userResponseDto).usingRecursiveComparison().
                ignoringFields("id", "userType").isEqualTo(userRequestDto);
        assertThat(userResponseDto.getId()).isNotNull();
        assertThat(userResponseDto.getUserType()).isEqualTo(UserType.SELLER);
    }

    @Test
    void shouldThrowEntityExistsExceptionWhenCpfCnpjAlreadyExists() {
        userService.createUser(userRequestDto);

        Throwable e = assertThrows(EntityExistsException.class,
                () -> userService.createUser(userRequestDto));
        assertEquals("User CPF/CNPJ already exists", e.getMessage());
    }

    @Test
    void shouldThrowEntityExistsExceptionWhenEmailAlreadyExists() {
        userService.createUser(userRequestDto);
        userRequestDto.setCpfCnpj(VALID_CNPJ);

        Throwable e = assertThrows(EntityExistsException.class,
                () -> userService.createUser(userRequestDto));
        assertEquals("User email already exists", e.getMessage());
    }

    @Test
    void shouldThrowInvalidUserTypeExceptionWhenCpfCnpjIsInvalid() {
        userRequestDto.setCpfCnpj("invalidCpfCnpj");

        Throwable e = assertThrows(InvalidUserTypeException.class,
                () -> userService.createUser(userRequestDto));
        assertEquals("Invalid CPF/CNPJ", e.getMessage());
    }
}