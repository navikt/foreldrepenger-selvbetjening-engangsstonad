package no.nav.foreldrepenger.selvbetjening.innsending.tjeneste;

import no.nav.foreldrepenger.selvbetjening.felles.attachments.Image2PDFConverter;
import no.nav.foreldrepenger.selvbetjening.felles.attachments.exceptions.AttachmentConversionException;
import no.nav.foreldrepenger.selvbetjening.innsending.json.Engangsstønad;
import no.nav.foreldrepenger.selvbetjening.innsending.json.Kvittering;
import no.nav.foreldrepenger.selvbetjening.innsending.json.Søknad;
import no.nav.foreldrepenger.selvbetjening.innsending.tjeneste.json.EngangsstønadDto;
import no.nav.foreldrepenger.selvbetjening.innsending.tjeneste.json.SøknadDto;
import no.nav.foreldrepenger.selvbetjening.oppslag.tjeneste.Oppslag;
import no.nav.foreldrepenger.selvbetjening.oppslag.tjeneste.json.PersonDto;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.ws.rs.BadRequestException;
import java.io.IOException;
import java.net.URI;

import static java.time.LocalDateTime.now;
import static java.util.Arrays.stream;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@ConditionalOnProperty(name = "stub.mottak", havingValue = "false", matchIfMissing = true)
public class Innsendingstjeneste implements Innsending {

    private static final Logger LOG = getLogger(Innsendingstjeneste.class);

    private final Image2PDFConverter converter;
    private final URI mottakServiceUrl;
    private final RestTemplate template;
    private final Oppslag oppslag;

    public Innsendingstjeneste(@Value("${FPSOKNAD_MOTTAK_API_URL}") URI baseUri, RestTemplate template, Oppslag oppslag, Image2PDFConverter converter) {
        this.mottakServiceUrl = mottakUriFra(baseUri);
        this.template = template;
        this.oppslag = oppslag;
        this.converter = converter;
    }

    @Override
    public ResponseEntity<Kvittering> sendInn(Søknad søknad, MultipartFile[] vedlegg) {
        LOG.info("Poster søknad til {}", mottakServiceUrl);
        søknad.opprettet = now();
        return post(søknad, vedlegg);
    }

    private ResponseEntity<Kvittering> post(Søknad søknad, MultipartFile... vedlegg) {
        return template.postForEntity(mottakServiceUrl, body(søknad, oppslag.hentPerson(), vedlegg), Kvittering.class);
    }

    private HttpEntity<SøknadDto> body(@RequestBody Søknad søknad, PersonDto person, MultipartFile... vedlegg) {
        SøknadDto dto;
        if (søknad instanceof Engangsstønad) {
            dto = new EngangsstønadDto((Engangsstønad) søknad, person);
        } else {
            throw new BadRequestException("Unknown application type");
        }

        stream(vedlegg)
                .map(this::vedleggBytes)
                .map(converter::convert)
                .forEach(dto::addVedlegg);

        return new HttpEntity<>(dto);
    }

    private static URI mottakUriFra(URI baseUri) {
        return UriComponentsBuilder
                .fromUri(baseUri)
                .path("/mottak/dokmot/send")
                .build().toUri();
    }

    private byte[] vedleggBytes(MultipartFile vedlegg) {
        try {
            return vedlegg.getBytes();
        } catch (IOException e) {
            throw new AttachmentConversionException("Kunne ikke hente bytes fra vedlegg " + vedlegg.getName(), e);
        }
    }
}
