package org.task.util;

import lombok.Getter;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.stereotype.Service;

@Service
@Getter
public class HttpClientService {
    private final CloseableHttpClient httpClient;

    public HttpClientService() {
        this.httpClient = HttpClients.createDefault();
    }
}
