package no.nav.foreldrepenger.selvbetjening.innsending;

import static no.nav.foreldrepenger.selvbetjening.felles.util.EnvUtil.CONFIDENTIAL;
import static no.nav.foreldrepenger.selvbetjening.innsending.InnsendingController.REST_SOKNAD;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import javax.inject.Inject;

import no.nav.foreldrepenger.selvbetjening.innsending.json.Ettersending;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import no.nav.foreldrepenger.selvbetjening.felles.attachments.exceptions.AttachmentsTooLargeException;
import no.nav.foreldrepenger.selvbetjening.felles.storage.Storage;
import no.nav.foreldrepenger.selvbetjening.felles.storage.StorageCrypto;
import no.nav.foreldrepenger.selvbetjening.felles.util.FnrExtractor;
import no.nav.foreldrepenger.selvbetjening.innsending.json.Kvittering;
import no.nav.foreldrepenger.selvbetjening.innsending.json.Søknad;
import no.nav.foreldrepenger.selvbetjening.innsending.json.Vedlegg;
import no.nav.foreldrepenger.selvbetjening.innsending.tjeneste.Innsending;
import no.nav.security.oidc.context.OIDCRequestContextHolder;


@RestController
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@RequestMapping(REST_SOKNAD)
public class InnsendingController {

    private static final Logger LOG = LoggerFactory.getLogger(InnsendingController.class);

    public static final String REST_SOKNAD = "/rest/soknad";

    private static final double MB = 1024 * 1024;
    private static final double MAX_VEDLEGG_SIZE = 7.5 * MB;

    private final Innsending innsending;

    @Inject
    public RestTemplate http;

    @Inject
    private OIDCRequestContextHolder contextHolder;

    @Inject
    private Storage storage;

    @Inject
    private StorageCrypto crypto;

    @Inject
    public InnsendingController(Innsending innsending) {
        this.innsending = innsending;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Kvittering> sendInn(@RequestBody Søknad søknad) {
        LOG.info(CONFIDENTIAL, "Mottok søknad  {}", søknad);
        søknad.vedlegg.forEach(this::fetchAttachment);
        checkVedleggTooLarge(søknad.vedlegg);
        ResponseEntity<Kvittering> respons = innsending.sendInn(søknad);
        deleteFromTempStorage(FnrExtractor.extract(contextHolder), søknad);
        return respons;
    }

    @PostMapping(path = "/ettersend", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Kvittering> sendInn(@RequestBody Ettersending ettersending) {
        LOG.info(CONFIDENTIAL, "Mottok ettersending  {}", ettersending);
        ettersending.vedlegg.forEach(this::fetchAttachment);
        checkVedleggTooLarge(ettersending.vedlegg);
        ResponseEntity<Kvittering> respons = innsending.sendInn(ettersending);
        ettersending.vedlegg.forEach(this::fetchAndDeleteAttachment);
        return respons;
    }

    private void checkVedleggTooLarge(List<Vedlegg> vedlegg) {
        long total = vedlegg.stream()
                .filter(v -> v.content != null)
                .mapToLong(v -> v.content.length)
                .sum();
        if (total > MAX_VEDLEGG_SIZE) {
            throw new AttachmentsTooLargeException("Samlet filstørrelse for alle vedlegg er " + total
                    + ", men kan ikke overstige " + MAX_VEDLEGG_SIZE + " bytes");
        }
    }

    private void fetchAttachment(Vedlegg vedlegg) {
        if (vedlegg.url != null) {
            vedlegg.content = http.getForObject(vedlegg.url, byte[].class);
        }
    }

    private void fetchAndDeleteAttachment(Vedlegg vedlegg) {
        if (vedlegg.url != null) {
            vedlegg.content = http.getForObject(vedlegg.url, byte[].class);
            http.delete(vedlegg.url);
        }
    }

    private void deleteFromTempStorage(String fnr, Søknad søknad) {
        søknad.vedlegg.forEach(this::fetchAndDeleteAttachment);
        storage.delete(crypto.encryptDirectoryName(fnr), "soknad");
    }
}