package com.soaresdev.picpaytestjr.v1.services;

import com.soaresdev.picpaytestjr.v1.dtos.externalApisDto.authorize.AuthorizeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import reactor.core.publisher.Mono;
import java.util.Objects;

@Service
public class AuthorizationService {
    private final Logger logger = LoggerFactory.getLogger(AuthorizationService.class.getName());
    private final String url;
    private final RestClient restClient;

    public AuthorizationService(@Value("${external.api.base-url}") String externalApiBaseUrl, RestClient.Builder restClientBuilder) {
        this.url = externalApiBaseUrl + "/api/v2/authorize";
        this.restClient = restClientBuilder.baseUrl(url).build();
    }

    public boolean isAuthorized() {
        logger.info("Checking if authorized via external API call to {}...", url);
        return Objects.requireNonNull(restClient.get().
                retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        (request, response) -> Mono.empty()).
                body(AuthorizeDto.class), "Null API response").data().authorization().booleanValue();
    }
}