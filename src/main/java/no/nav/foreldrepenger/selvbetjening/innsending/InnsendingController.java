package no.nav.foreldrepenger.selvbetjening.innsending;

import no.nav.foreldrepenger.selvbetjening.felles.attachments.exceptions.AttachmentsTooLargeException;
import no.nav.foreldrepenger.selvbetjening.innsending.json.Kvittering;
import no.nav.foreldrepenger.selvbetjening.innsending.json.Søknad;
import no.nav.foreldrepenger.selvbetjening.innsending.tjeneste.Innsending;
import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;

import static java.util.Arrays.stream;
import static no.nav.foreldrepenger.selvbetjening.innsending.InnsendingController.REST_ENGANGSSTONAD;
import static no.nav.foreldrepenger.selvbetjening.innsending.InnsendingController.REST_SOKNAD;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = {"acr=Level4"})
@RequestMapping({REST_SOKNAD, REST_ENGANGSSTONAD})
public class InnsendingController {

    public static final String REST_ENGANGSSTONAD = "/rest/engangsstonad"; // TODO: Fjern denne når frontend er oppdatert
    public static final String REST_SOKNAD = "/rest/soknad";

    private static final double MB = 1024 * 1024;
    private static final double MAX_VEDLEGG_SIZE = 7.5 * MB;

    private final Innsending innsending;

    @Inject
    public InnsendingController(Innsending innsending) {
        this.innsending = innsending;
    }

    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Kvittering> sendInn(@RequestPart("soknad") Søknad søknad, @RequestPart("vedlegg") MultipartFile... vedlegg) throws Exception {
        checkVedleggTooLarge(vedlegg);

        return innsending.sendInn(søknad, vedlegg);
    }

    private void checkVedleggTooLarge(MultipartFile... vedlegg) {
        long total = stream(vedlegg)
                .mapToLong(MultipartFile::getSize)
                .sum();
        if (total > MAX_VEDLEGG_SIZE) {
            throw new AttachmentsTooLargeException("Samlet filstørrelse for alle vedlegg er " + total + ", men kan ikke overstige " + MAX_VEDLEGG_SIZE + " bytes");
        }
    }
}
