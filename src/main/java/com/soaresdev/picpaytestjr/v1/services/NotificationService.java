package com.soaresdev.picpaytestjr.v1.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class NotificationService {
    private static final String URL = "https://util.devi.tools/api/v1/notify";
    private final RestClient restClient;

    public NotificationService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(URL).build();
    }

    public void notifyUser() {
        restClient.post().retrieve().toBodilessEntity();
    }
}