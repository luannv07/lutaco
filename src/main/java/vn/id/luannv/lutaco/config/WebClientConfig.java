package vn.id.luannv.lutaco.config;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebClientConfig {
    @NonFinal
    @Value("${payment.base-url}")
    String baseUrl;
    @NonFinal
    @Value("${payment.x-client-id}")
    String xClientId;
    @NonFinal
    @Value("${payment.x-api-key}")
    String xApiKey;

    @Bean
    @Qualifier("payOsWebClient")
    public WebClient payOsWebClient() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("x-client-id", xClientId);
        headers.add("x-api-key", xApiKey);

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(httpHeaders -> httpHeaders.addAll(headers))
                .build();
    }
}
