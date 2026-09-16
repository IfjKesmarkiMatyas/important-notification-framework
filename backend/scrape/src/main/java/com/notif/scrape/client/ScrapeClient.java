package com.notif.scrape.client;

import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import com.notif.common.dto.scrape.FetchResult;

@Component
public class ScrapeClient {

    private final RestClient restClient;

    public ScrapeClient(RestClient.Builder restClientBuilder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(20));
        this.restClient = restClientBuilder.requestFactory(factory).build();
    }

    public FetchResult get(String url) {
        try {
            var entity = restClient.get()
                    .uri(url)
                    .header(HttpHeaders.USER_AGENT, "Notif/0.1")
                    .header(HttpHeaders.ACCEPT, "application/rss+xml, application/json, application/geo+json, */*")
                    .retrieve()
                    .toEntity(String.class);
            MediaType type = entity.getHeaders().getContentType();
            return FetchResult.ok(
                    entity.getStatusCode().value(),
                    type == null ? null : type.toString(),
                    entity.getBody()
            );
        } catch (RestClientResponseException ex) {
            return FetchResult.fail(ex.getStatusCode().value(), ex.getResponseBodyAsString(), ex.getMessage());
        } catch (Exception ex) {
            return FetchResult.fail(null, null, ex.getMessage() == null ? "fetch failed" : ex.getMessage());
        }
    }
}
