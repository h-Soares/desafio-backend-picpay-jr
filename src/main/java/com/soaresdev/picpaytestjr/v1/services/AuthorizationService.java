package com.soaresdev.picpaytestjr.v1.services;

import com.soaresdev.picpaytestjr.exceptions.TransferException;
import com.soaresdev.picpaytestjr.v1.dtos.externalApisDto.authorize.AuthorizeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Objects;

@Service
public class AuthorizationService {
    private final Logger logger = LoggerFactory.getLogger(AuthorizationService.class.getName());
    private static final String URL = "https://util.devi.tools/api/v2/authorize";
    private final RestClient restClient;

    public AuthorizationService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(URL).build();
    }

    public boolean isAuthorized() {
        logger.info("Checking if authorized via external API call to {}...", URL);
        return Objects.requireNonNull(restClient.get().
                retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new TransferException("Transfer not authorized");
                }).
                body(AuthorizeDto.class), "Null API response").data().authorization().booleanValue();
    }
}