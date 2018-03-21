package no.nav.foreldrepenger.selvbetjening.config;

import com.google.common.collect.ImmutableMap;
import no.nav.foreldrepenger.selvbetjening.rest.util.ApiKeyInjectingClientInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.OrderedHealthAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.inject.Inject;
import java.net.URI;

import static java.util.Arrays.asList;

@Configuration
public class ApiConfiguration implements WebMvcConfigurer {

    @Value("${apikeys.key:x-nav-apiKey}")
    private String key;

    @Value("${FPSOKNAD_MOTTAK_API_URL}")
    private URI mottakServiceUri;

    @Value("${FPSOKNAD_OPPSLAG_API_URL}")
    private URI oppslagServiceUri;

    @Value("${FORELDREPENGESOKNAD_API_FPSOKNAD_MOTTAK_API_APIKEY_PASSWORD}")
    String mottakApiKey;

    @Value("${FORELDREPENGESOKNAD_API_FPSOKNAD_OPPSLAG_API_APIKEY_PASSWORD}")
    String oppslagApiKey;

    @Inject
    CorsInterceptor corsInterceptor;

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestInterceptor... interceptors) {
        RestTemplate template = new RestTemplate();
        template.setInterceptors(asList(interceptors));
        return template;
    }

    @Bean
    public ClientHttpRequestInterceptor apiKeyInjectingClientInterceptor() {
        return new ApiKeyInjectingClientInterceptor(key,
                ImmutableMap.<URI, String>builder()
                        .put(mottakServiceUri, mottakApiKey)
                        .put(oppslagServiceUri, oppslagApiKey)
                        .build());

    }

    @Bean
    public HealthAggregator healthAggregator() {
        return new OrderedHealthAggregator();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(corsInterceptor);
    }
}
