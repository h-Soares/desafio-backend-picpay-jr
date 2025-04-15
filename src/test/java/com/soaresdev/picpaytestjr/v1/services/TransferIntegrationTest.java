package com.soaresdev.picpaytestjr.v1.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.soaresdev.picpaytestjr.entities.User;
import com.soaresdev.picpaytestjr.entities.enums.UserType;
import com.soaresdev.picpaytestjr.exceptions.TransferException;
import com.soaresdev.picpaytestjr.repositories.TransferRepository;
import com.soaresdev.picpaytestjr.repositories.UserRepository;
import com.soaresdev.picpaytestjr.v1.dtos.TransferDto;
import com.soaresdev.picpaytestjr.v1.dtos.UserRequestDto;
import com.soaresdev.picpaytestjr.v1.dtos.externalApisDto.authorize.AuthorizeDto;
import com.soaresdev.picpaytestjr.v1.dtos.externalApisDto.authorize.DataDto;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransferIntegrationTest {
    private static final String URL_PATH = "/v1/transfer";
    private static final String POSTGRESQL_IMAGE = "postgres:17.4";
    private static final String VALID_CPF = "47776629911";
    private static final String VALID_CNPJ = "79610519000141";
    private static final String VALID_COSTUMER_EMAIL = "johndoe@testing.com";
    private static final String VALID_SELLER_EMAIL = "marydoe@testing.com";
    private final UserRequestDto userCostumerRequestDto;
    private final UserRequestDto userSellerRequestDto;
    private final TransferDto transferDto;
    private final AuthorizeDto validAuthorizeDto;
    private final AuthorizeDto invalidAuthorizeDto;

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TransferRepository transferRepository;

    public TransferIntegrationTest() {
        this.userCostumerRequestDto = new UserRequestDto();
        this.userSellerRequestDto = new UserRequestDto();
        this.transferDto = new TransferDto();
        this.validAuthorizeDto = new AuthorizeDto("success", new DataDto(Boolean.TRUE));
        this.invalidAuthorizeDto = new AuthorizeDto("fail", new DataDto(Boolean.FALSE));
    }

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance().
            options(wireMockConfig().dynamicPort()).build();

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
        registry.add("external.api.base-url", wireMockServer::baseUrl);
    }

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        transferRepository.deleteAll();
        userRepository.deleteAll();
        setupStandardTransfer();
    }

    @Test
    void shouldReturn204WhenTransfer() throws JsonProcessingException {
        initUserInstancesOnDatabase();
        setAuthorizationByExternalApis(validAuthorizeDto);

        given().
                contentType(ContentType.JSON).
                body(objectMapper.writeValueAsString(transferDto)).
        when().
                post(URL_PATH).
        then().
                statusCode(HttpStatus.NO_CONTENT.value());

        User userCostumer = userRepository.findByEmail(userCostumerRequestDto.getEmail()).orElseThrow(EntityNotFoundException::new);
        User userSeller = userRepository.findByEmail(userSellerRequestDto.getEmail()).orElseThrow(EntityNotFoundException::new);
        assertEquals(userCostumerRequestDto.getBalance().subtract(transferDto.getAmount()).stripTrailingZeros(), userCostumer.getBalance().stripTrailingZeros());
        assertEquals(userSellerRequestDto.getBalance().add(transferDto.getAmount()).stripTrailingZeros(), userSeller.getBalance().stripTrailingZeros());
    }

    @Test
    void shouldReturn422WhenTransferIsNotAuthorized() throws JsonProcessingException {
        initUserInstancesOnDatabase();
        setAuthorizationByExternalApis(invalidAuthorizeDto);

        given().
                contentType(ContentType.JSON).
                body(objectMapper.writeValueAsString(transferDto)).
        when().
                post(URL_PATH).
        then().
                statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value()).
                contentType(ContentType.JSON).
                body("timestamp", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z")).
                body("status", equalTo(HttpStatus.UNPROCESSABLE_ENTITY.value())).
                body("error", equalTo(TransferException.class.getSimpleName())).
                body("message", equalTo("Transfer not authorized")).
                body("path",equalTo(URL_PATH));

        User userCostumer = userRepository.findByEmail(userCostumerRequestDto.getEmail()).orElseThrow(EntityNotFoundException::new);
        User userSeller = userRepository.findByEmail(userSellerRequestDto.getEmail()).orElseThrow(EntityNotFoundException::new);
        assertEquals(0, userCostumer.getBalance().compareTo(userCostumerRequestDto.getBalance()));
        assertEquals(0, userSeller.getBalance().compareTo(userSellerRequestDto.getBalance()));
    }

    @Test
    void shouldReturn404WhenUserDoesNotExist() throws JsonProcessingException {
        initUserInstancesOnDatabase();
        transferDto.setPayerEmail("invalidcostumer@email.com");
        transferDto.setPayeeEmail("invalidseller@email.com");
        setAuthorizationByExternalApis(validAuthorizeDto);

        given().
                contentType(ContentType.JSON).
                body(objectMapper.writeValueAsString(transferDto)).
        when().
                post(URL_PATH).
        then().
                statusCode(HttpStatus.NOT_FOUND.value()).
                contentType(ContentType.JSON).
                body("timestamp", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z")).
                body("status", equalTo(HttpStatus.NOT_FOUND.value())).
                body("error", equalTo(EntityNotFoundException.class.getSimpleName())).
                body("message", equalTo("User not found")).
                body("path",equalTo(URL_PATH));
    }

    @Test
    void shouldReturn422WhenTryToTransferAsASeller() throws JsonProcessingException {
        initUserInstancesOnDatabase();
        transferDto.setPayerEmail(VALID_SELLER_EMAIL);
        transferDto.setPayeeEmail(VALID_COSTUMER_EMAIL);
        setAuthorizationByExternalApis(validAuthorizeDto);

        given().
                contentType(ContentType.JSON).
                body(objectMapper.writeValueAsString(transferDto)).
        when().
                post(URL_PATH).
        then().
                statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value()).
                contentType(ContentType.JSON).
                body("timestamp", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z")).
                body("status", equalTo(HttpStatus.UNPROCESSABLE_ENTITY.value())).
                body("error", equalTo(TransferException.class.getSimpleName())).
                body("message", equalTo("You are not allowed to transfer as a seller")).
                body("path",equalTo(URL_PATH));

        User userCostumer = userRepository.findByEmail(userCostumerRequestDto.getEmail()).orElseThrow(EntityNotFoundException::new);
        User userSeller = userRepository.findByEmail(userSellerRequestDto.getEmail()).orElseThrow(EntityNotFoundException::new);
        assertEquals(0, userCostumer.getBalance().compareTo(userCostumerRequestDto.getBalance()));
        assertEquals(0, userSeller.getBalance().compareTo(userSellerRequestDto.getBalance()));
    }

    @Test
    void shouldReturn422WhenTryToTransferWithoutEnoughMoney() throws JsonProcessingException {
        initUserInstancesOnDatabase();
        setAuthorizationByExternalApis(validAuthorizeDto);
        transferDto.setAmount(userCostumerRequestDto.getBalance().add(BigDecimal.ONE));

        given().
                contentType(ContentType.JSON).
                body(objectMapper.writeValueAsString(transferDto)).
        when().
                post(URL_PATH).
        then().
                statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value()).
                contentType(ContentType.JSON).
                body("timestamp", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z")).
                body("status", equalTo(HttpStatus.UNPROCESSABLE_ENTITY.value())).
                body("error", equalTo(TransferException.class.getSimpleName())).
                body("message", equalTo("You do not have enough money to transfer")).
                body("path",equalTo(URL_PATH));

        User userCostumer = userRepository.findByEmail(userCostumerRequestDto.getEmail()).orElseThrow(EntityNotFoundException::new);
        User userSeller = userRepository.findByEmail(userSellerRequestDto.getEmail()).orElseThrow(EntityNotFoundException::new);
        assertEquals(0, userCostumer.getBalance().compareTo(userCostumerRequestDto.getBalance()));
        assertEquals(0, userSeller.getBalance().compareTo(userSellerRequestDto.getBalance()));
    }

    @Test
    void shouldReturn422WhenTryToTransferToItself() throws JsonProcessingException {
        initUserInstancesOnDatabase();
        setAuthorizationByExternalApis(validAuthorizeDto);
        transferDto.setPayeeEmail(transferDto.getPayerEmail());

        given().
                contentType(ContentType.JSON).
                body(objectMapper.writeValueAsString(transferDto)).
        when().
                post(URL_PATH).
        then().
                statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value()).
                contentType(ContentType.JSON).
                body("timestamp", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z")).
                body("status", equalTo(HttpStatus.UNPROCESSABLE_ENTITY.value())).
                body("error", equalTo(TransferException.class.getSimpleName())).
                body("message", equalTo("You are not allowed to transfer to yourself")).
                body("path",equalTo(URL_PATH));

        User userCostumer = userRepository.findByEmail(userCostumerRequestDto.getEmail()).orElseThrow(EntityNotFoundException::new);
        User userSeller = userRepository.findByEmail(userSellerRequestDto.getEmail()).orElseThrow(EntityNotFoundException::new);
        assertEquals(0, userCostumer.getBalance().compareTo(userCostumerRequestDto.getBalance()));
        assertEquals(0, userSeller.getBalance().compareTo(userSellerRequestDto.getBalance()));
    }

    @Test
    void shouldReturn400WhenSendInvalidTransfer() throws JsonProcessingException {
        setAuthorizationByExternalApis(validAuthorizeDto);
        TransferDto invalidTransferDto = new TransferDto("invalid-payer-email", "invalid-payee-email", BigDecimal.ZERO);

        given().
                contentType(ContentType.JSON).
                body(objectMapper.writeValueAsString(invalidTransferDto)).
        when().
                post(URL_PATH).
        then().
                statusCode(HttpStatus.BAD_REQUEST.value()).
                contentType(ContentType.JSON).
                body("timestamp", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{9}Z")).
                body("status", equalTo(HttpStatus.BAD_REQUEST.value())).
                body("errors.size()", equalTo(3)).
                body("errors[0]", equalTo("amount: Balance must be greater than zero")).
                body("errors[1]", equalTo("payeeEmail: Invalid payee email")).
                body("errors[2]", equalTo("payerEmail: Invalid payer email")).
                body("path",equalTo(URL_PATH));
    }

    private void initUserInstancesOnDatabase() {
        userCostumerRequestDto.setBalance(BigDecimal.TEN);
        userCostumerRequestDto.setCpfCnpj(VALID_CPF);
        userCostumerRequestDto.setEmail(VALID_COSTUMER_EMAIL);
        userCostumerRequestDto.setFullName("John Doe");
        userCostumerRequestDto.setPassword("the-strongest-password123");
        userRepository.save(new User(UserType.CUSTOMER.getCode(), userCostumerRequestDto.getPassword(), userCostumerRequestDto.getFullName(), userCostumerRequestDto.getEmail(), userCostumerRequestDto.getCpfCnpj(), userCostumerRequestDto.getBalance()));

        userSellerRequestDto.setBalance(BigDecimal.ONE);
        userSellerRequestDto.setCpfCnpj(VALID_CNPJ);
        userSellerRequestDto.setEmail(VALID_SELLER_EMAIL);
        userSellerRequestDto.setFullName("Mary Doe");
        userSellerRequestDto.setPassword("the-strongest-password12345");
        userRepository.save(new User(UserType.SELLER.getCode(), userSellerRequestDto.getPassword(), userSellerRequestDto.getFullName(), userSellerRequestDto.getEmail(), userSellerRequestDto.getCpfCnpj(), userSellerRequestDto.getBalance()));
    }

    private void setupStandardTransfer() {
        transferDto.setAmount(BigDecimal.valueOf(7));
        transferDto.setPayerEmail(VALID_COSTUMER_EMAIL);
        transferDto.setPayeeEmail(VALID_SELLER_EMAIL);
    }

    private void setAuthorizationByExternalApis(AuthorizeDto validAuthorizeDto) throws JsonProcessingException {
        wireMockServer.stubFor(get(urlEqualTo("/api/v2/authorize")).
                willReturn(aResponse().withStatus(HttpStatus.OK.value()).
                        withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).
                        withBody(objectMapper.writeValueAsString(validAuthorizeDto))));

        wireMockServer.stubFor(post(urlEqualTo("/api/v1/notify")).
                willReturn(aResponse().withStatus(HttpStatus.OK.value())));
    }
}