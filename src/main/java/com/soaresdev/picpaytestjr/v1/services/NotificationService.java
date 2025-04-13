package com.soaresdev.picpaytestjr.v1.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class NotificationService {
    private final Logger logger = LoggerFactory.getLogger(NotificationService.class.getName());
    private static final String URL = "https://util.devi.tools/api/v1/notify";
    private final RestClient restClient;

    public NotificationService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(URL).build();
    }

    public void notifyUser() {
        logger.info("Sending user notification via external API call to {}...", URL);
        restClient.post().retrieve().toBodilessEntity();
    }
}