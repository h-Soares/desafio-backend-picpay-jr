package com.soaresdev.picpaytestjr.v1.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soaresdev.picpaytestjr.entities.User;
import com.soaresdev.picpaytestjr.entities.enums.UserType;
import com.soaresdev.picpaytestjr.repositories.UserRepository;
import com.soaresdev.picpaytestjr.v1.dtos.UserRequestDto;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserIntegrationTest {
    private static final String URL_PATH = "/v1/user";
    private static final String POSTGRESQL_IMAGE = "postgres:17.4";
    private static final String VALID_CPF = "47776629911";
    private static final String VALID_CNPJ = "79610519000141";
    private final UserRequestDto userRequestDto;

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;

    public UserIntegrationTest() {
        this.userRequestDto = new UserRequestDto();
    }

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        userRepository.deleteAll();
        setupStandardUser();
    }

    @Container
    private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>(POSTGRESQL_IMAGE)
            .withDatabaseName("testing")
            .withUsername("testing_user")
            .withPassword("testing_password");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRE_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRE_SQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRE_SQL_CONTAINER::getPassword);
    }

    @Test
    @Order(1)
    void shouldReturn201WhenCreateCustomer() throws JsonProcessingException {
        given().
                contentType(ContentType.JSON).
                body(objectMapper.writeValueAsString(userRequestDto)).
        when().
                post(URL_PATH).
        then().
                statusCode(HttpStatus.CREATED.value()).
                contentType(ContentType.JSON).
                body("id", notNullValue()).
                body("fullName", equalTo(userRequestDto.getFullName())).
                body("userType", equalTo(UserType.CUSTOMER.name())).
                body("cpfCnpj", equalTo(userRequestDto.getCpfCnpj())).
                body("email",equalTo(userRequestDto.getEmail())).
                body("balance.toString()",equalTo(userRequestDto.getBalance().stripTrailingZeros().toPlainString())).
                header("Location", containsString(URL_PATH + "/" + userRequestDto.getEmail()));
    }

    @Test
    @Order(2)
    void shouldReturn201WhenCreateSeller() throws JsonProcessingException {
        userRequestDto.setCpfCnpj(VALID_CNPJ);

        given().
                contentType(ContentType.JSON).
                body(objectMapper.writeValueAsString(userRequestDto)).
        when().
                post(URL_PATH).
        then().
                statusCode(HttpStatus.CREATED.value()).
                contentType(ContentType.JSON).
                body("id", notNullValue()).
                body("fullName", equalTo(userRequestDto.getFullName())).
                body("userType", equalTo(UserType.SELLER.name())).
                body("cpfCnpj", equalTo(userRequestDto.getCpfCnpj())).
                body("email",equalTo(userRequestDto.getEmail())).
                body("balance.toString()",equalTo(userRequestDto.getBalance().stripTrailingZeros().toPlainString())).
                header("Location", containsString(URL_PATH + "/" + userRequestDto.getEmail()));
    }

    @Test
    void shouldReturn409WhenCpfCnpjAlreadyExists() throws JsonProcessingException {
        saveUserOnDatabase(userRequestDto);

        given().
                contentType(ContentType.JSON).
                body(objectMapper.writeValueAsString(userRequestDto)).
        when().
                post(URL_PATH).
        then().
                statusCode(HttpStatus.CONFLICT.value()).
                contentType(ContentType.JSON).
                body("timestamp", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z")).
                body("status", equalTo(HttpStatus.CONFLICT.value())).
                body("error", equalTo(EntityExistsException.class.getSimpleName())).
                body("message", equalTo("User CPF/CNPJ already exists")).
                body("path",equalTo(URL_PATH)).
                header("Location", nullValue());
    }

    @Test
    void shouldReturn409WhenEmailAlreadyExists() throws JsonProcessingException {
        saveUserOnDatabase(userRequestDto);
        userRequestDto.setCpfCnpj(VALID_CNPJ);

        given().
                contentType(ContentType.JSON).
                body(objectMapper.writeValueAsString(userRequestDto)).
        when().
                post(URL_PATH).
        then().
                statusCode(HttpStatus.CONFLICT.value()).
                contentType(ContentType.JSON).
                body("timestamp", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z")).
                body("status", equalTo(HttpStatus.CONFLICT.value())).
                body("error", equalTo(EntityExistsException.class.getSimpleName())).
                body("message", equalTo("User email already exists")).
                body("path",equalTo(URL_PATH)).
                header("Location", nullValue());
    }

    @Test
    void shouldReturn400WhenSendInvalidUser() throws JsonProcessingException {
        setUserRequestToInvalid();

        given().
                contentType(ContentType.JSON).
                body(objectMapper.writeValueAsString(userRequestDto)).
        when().
                post(URL_PATH).
        then().
                statusCode(HttpStatus.BAD_REQUEST.value()).
                contentType(ContentType.JSON).
                body("timestamp", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z")).
                body("status", equalTo(HttpStatus.BAD_REQUEST.value())).
                body("errors.size()", equalTo(5)).
                body("errors[0]", equalTo("balance: Balance must be greater than zero")).
                body("errors[1]", equalTo("cpfCnpj: Invalid CPF/CNPJ")).
                body("errors[2]", equalTo("email: Invalid email")).
                body("errors[3]", equalTo("fullName: Invalid name")).
                body("errors[4]", equalTo("password: Password must contain at least 6 characters, with at least one number")).
                body("path",equalTo(URL_PATH)).
                header("Location", nullValue());
    }

    @Test
    void shouldReturn200WhenFindAllUsers() {
        UserRequestDto userRequestDtoTwo = new UserRequestDto(BigDecimal.TEN, VALID_CNPJ, "seller@testing.com", "The Test", "testing123");
        saveUserOnDatabase(userRequestDto);
        saveUserOnDatabase(userRequestDtoTwo);

        given().
                get(URL_PATH).
        then().
                statusCode(HttpStatus.OK.value()).
                contentType(ContentType.JSON).
                body("content.size()", equalTo(2)).
                body("content[0].id", notNullValue()).
                body("content[0].fullName", equalTo(userRequestDto.getFullName())).
                body("content[0].userType", equalTo(UserType.CUSTOMER.name())).
                body("content[0].cpfCnpj", equalTo(userRequestDto.getCpfCnpj())).
                body("content[0].email",equalTo(userRequestDto.getEmail())).
                body("content[0].balance",equalTo(userRequestDto.getBalance().floatValue())).
                body("content[1].id", notNullValue()).
                body("content[1].fullName", equalTo(userRequestDtoTwo.getFullName())).
                body("content[1].userType", equalTo(UserType.SELLER.name())).
                body("content[1].cpfCnpj", equalTo(userRequestDtoTwo.getCpfCnpj())).
                body("content[1].email",equalTo(userRequestDtoTwo.getEmail())).
                body("content[1].balance",equalTo(userRequestDtoTwo.getBalance().floatValue()));
    }

    @Test
    void shouldReturn200WhenFindUserByEmail() {
        saveUserOnDatabase(userRequestDto);

        given().
                pathParam("email", userRequestDto.getEmail()).
        when().
                get(URL_PATH + "/{email}").
        then().
                statusCode(HttpStatus.OK.value()).
                contentType(ContentType.JSON).
                body("id", notNullValue()).
                body("fullName", equalTo(userRequestDto.getFullName())).
                body("userType", equalTo(UserType.CUSTOMER.name())).
                body("cpfCnpj", equalTo(userRequestDto.getCpfCnpj())).
                body("email",equalTo(userRequestDto.getEmail())).
                body("balance",equalTo(userRequestDto.getBalance().floatValue()));
    }

    @Test
    void shouldReturn404WhenEmailDoesNotExist() {
        String encodedUserRequestEmail = URLEncoder.encode(userRequestDto.getEmail(), StandardCharsets.UTF_8);
        given().
                pathParam("email", userRequestDto.getEmail()).
        when().
                get(URL_PATH + "/{email}").
        then().
                statusCode(HttpStatus.NOT_FOUND.value()).
                contentType(ContentType.JSON).
                body("timestamp", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z")).
                body("status", equalTo(HttpStatus.NOT_FOUND.value())).
                body("error", equalTo(EntityNotFoundException.class.getSimpleName())).
                body("message", equalTo("User not found")).
                body("path",equalTo(URL_PATH + "/" + encodedUserRequestEmail));
    }

    private void setupStandardUser() {
        userRequestDto.setBalance(BigDecimal.ONE);
        userRequestDto.setCpfCnpj(VALID_CPF);
        userRequestDto.setEmail("john.doe@testing.com");
        userRequestDto.setFullName("John Doe");
        userRequestDto.setPassword("the-strongest-password123");
    }

    private void saveUserOnDatabase(UserRequestDto userRequestDto) {
        Integer userTypeCode = null;
        if(userRequestDto.getCpfCnpj().equals(VALID_CPF))
            userTypeCode = UserType.CUSTOMER.getCode();
        else if(userRequestDto.getCpfCnpj().equals(VALID_CNPJ))
            userTypeCode = UserType.SELLER.getCode();

        userRepository.save(new User(userTypeCode, userRequestDto.getPassword(), userRequestDto.getFullName(), userRequestDto.getEmail(), userRequestDto.getCpfCnpj(), userRequestDto.getBalance()));
    }

    private void setUserRequestToInvalid() {
        userRequestDto.setCpfCnpj("invalidCpfCnpj");
        userRequestDto.setEmail("invalidEmail");
        userRequestDto.setFullName("invalidFull!Name");
        userRequestDto.setPassword("invalidPassword");
        userRequestDto.setBalance(BigDecimal.ZERO);
    }
}