package com.soaresdev.picpaytestjr.v1.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class NotificationService {
    private final Logger logger = LoggerFactory.getLogger(NotificationService.class.getName());
    private final String url;
    private final RestClient restClient;

    public NotificationService(@Value("${external.api.base-url}") String externalApiBaseUrl, RestClient.Builder restClientBuilder) {
        this.url = externalApiBaseUrl + "/api/v1/notify";
        this.restClient = restClientBuilder.baseUrl(url).build();
    }

    public void notifyUser() {
        logger.info("Sending user notification via external API call to {}...", url);
        restClient.post().retrieve().toBodilessEntity();
    }
}