package no.nav.foreldrepenger.selvbetjening.config;

import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.selvbetjening.util.Constants.FNR;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.retry.RetryContext.NAME;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.web.client.RestOperations;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.google.common.collect.ImmutableMap;

import no.nav.foreldrepenger.selvbetjening.interceptors.client.ApiKeyInjectingClientInterceptor;
import no.nav.foreldrepenger.selvbetjening.tjeneste.ZoneCrossingAware;
import no.nav.foreldrepenger.selvbetjening.tjeneste.mellomlagring.StorageCrypto;

@Configuration
public class ApiConfiguration implements WebMvcConfigurer {
    @Value("${allowed.origins}")
    private String[] allowedOrigins;

    private static final Logger LOG = LoggerFactory.getLogger(ApiConfiguration.class);

    @Bean
    public RestOperations restTemplate(ClientHttpRequestInterceptor... interceptors) {
        LOG.info("Registrerer interceptorer {}", Arrays.toString(interceptors));
        return new RestTemplateBuilder()
                .interceptors(interceptors)
                .build();
    }

    @Bean
    public ClientHttpRequestInterceptor apiKeyInjectingClientInterceptor(ZoneCrossingAware... configs) {
        var builder = ImmutableMap.<URI, String>builder();
        Arrays.stream(configs)
                .forEach(c -> builder.put(c.zoneCrossingUri(), c.getKey()));
        return new ApiKeyInjectingClientInterceptor(builder.build());

    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

    }

    @Bean
    public List<RetryListener> retryListeners() {
        Logger log = LoggerFactory.getLogger(getClass());

        return singletonList(new RetryListenerSupport() {

            @Override
            public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
                    Throwable t) {
                log.warn("Retry methode {} kastet {}. exception {}",
                        context.getAttribute(NAME), context.getRetryCount(), t.toString(), t);
            }
        });
    }

    @Bean
    public StorageCrypto storageCrypto(@Value("${storage.passphrase}") String passPhrase) {
        return new StorageCrypto(passPhrase);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods(POST.name(), GET.name(), OPTIONS.name(), DELETE.name())
                .allowCredentials(true)
                .exposedHeaders(LOCATION)
                .allowedHeaders(FNR, CONTENT_TYPE)
                .allowedOrigins(allowedOrigins);
    }
}
