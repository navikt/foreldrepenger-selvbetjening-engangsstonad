package no.nav.foreldrepenger.selvbetjening.interceptors.client;

import static no.nav.foreldrepenger.selvbetjening.util.Constants.X_NAV_API_KEY;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class ApiKeyInjectingClientInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOG = getLogger(ApiKeyInjectingClientInterceptor.class);

    private final Map<URI, String> apiKeys;

    public ApiKeyInjectingClientInterceptor(Map<URI, String> apiKeys) {
        this.apiKeys = apiKeys;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        URI destination = request.getURI();
        Optional<String> apiKey = apiKeyFor(destination);
        if (apiKey.isPresent()) {
            LOG.trace("Injisert API-key som header {} for {}", X_NAV_API_KEY, destination);
            request.getHeaders().add(X_NAV_API_KEY, apiKey.get());
        } else {
            LOG.trace("Ingen API-key ble funnet for {} (sjekket {} konfigurasjoner)", destination,
                    apiKeys.values().size());
        }
        return execution.execute(request, body);
    }

    private Optional<String> apiKeyFor(URI uri) {
        return apiKeys.entrySet().stream()
                .filter(s -> uri.toString().startsWith(s.getKey().toString()))
                .map(Map.Entry::getValue)
                .findFirst();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [apiKeys=" + apiKeys.keySet() + "]";
    }

}
