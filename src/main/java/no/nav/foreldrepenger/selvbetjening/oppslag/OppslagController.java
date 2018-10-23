package no.nav.foreldrepenger.selvbetjening.oppslag;

import static no.nav.foreldrepenger.selvbetjening.felles.util.EnvUtil.CONFIDENTIAL;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import javax.inject.Inject;

import no.nav.security.oidc.api.ProtectedWithClaims;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import no.nav.foreldrepenger.selvbetjening.oppslag.json.Person;
import no.nav.foreldrepenger.selvbetjening.oppslag.json.Sak;
import no.nav.foreldrepenger.selvbetjening.oppslag.json.Søkerinfo;
import no.nav.foreldrepenger.selvbetjening.oppslag.tjeneste.Oppslag;
import no.nav.foreldrepenger.selvbetjening.oppslag.tjeneste.json.PersonDto;
import no.nav.foreldrepenger.selvbetjening.oppslag.tjeneste.json.SøkerinfoDto;

@RestController
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
public class OppslagController {

    public static final String REST_PERSONINFO = "/rest/personinfo";
    private static final String REST_SØKERINFO = "/rest/sokerinfo";
    private static final String REST_SAKER = "/rest/saker";

    private static final Logger LOG = getLogger(OppslagController.class);

    private final Oppslag oppslag;

    @Inject
    public OppslagController(Oppslag oppslag) {
        this.oppslag = oppslag;
    }

    @GetMapping(REST_PERSONINFO)
    public Person personinfo() {
        LOG.info("Henter personinfo...");
        PersonDto person = oppslag.hentPerson();
        LOG.info(CONFIDENTIAL, "Fikk personInfo {}", person);
        return new Person(person);
    }

    @GetMapping(REST_SØKERINFO)
    public Søkerinfo søkerinfo() {
        LOG.info("Henter søkerinfo...");
        SøkerinfoDto info = oppslag.hentSøkerinfo();
        LOG.info(CONFIDENTIAL, "Fikk søkerinfo {}", info);
        return new Søkerinfo(info);
    }

    @GetMapping(REST_SAKER)
    public List<Sak> saker() {
        LOG.info("Henter saker...");
        List<Sak> saker = oppslag.hentSaker();
        LOG.info(CONFIDENTIAL, "Fikk {} sak(er) {}", saker.size(), saker);
        return saker;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [oppslag=" + oppslag + "]";
    }

}
