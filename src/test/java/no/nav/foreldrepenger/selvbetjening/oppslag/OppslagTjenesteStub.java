package no.nav.foreldrepenger.selvbetjening.oppslag;

import static com.neovisionaries.i18n.CountryCode.NO;
import static java.time.LocalDate.now;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import no.nav.foreldrepenger.selvbetjening.innsyn.saker.Kjønn;
import no.nav.foreldrepenger.selvbetjening.innsyn.saker.Navn;
import no.nav.foreldrepenger.selvbetjening.oppslag.domain.AnnenForelder;
import no.nav.foreldrepenger.selvbetjening.oppslag.domain.Arbeidsforhold;
import no.nav.foreldrepenger.selvbetjening.oppslag.domain.Bankkonto;
import no.nav.foreldrepenger.selvbetjening.oppslag.domain.Barn;
import no.nav.foreldrepenger.selvbetjening.oppslag.domain.Person;
import no.nav.foreldrepenger.selvbetjening.oppslag.domain.Søkerinfo;
import no.nav.foreldrepenger.selvbetjening.oppslag.dto.PersonDto;

@Service("oppslagTjeneste")
@ConditionalOnProperty(name = "stub.oppslag", havingValue = "true")
public class OppslagTjenesteStub implements Oppslag {
    private static final Logger LOG = getLogger(OppslagTjenesteStub.class);

    @Override
    public String ping() {
        return "hello earthlings";
    }

    @Override
    public Person hentPerson() {
        LOG.info("Stubber oppslag...");
        return person();
    }

    @Override
    public Søkerinfo hentSøkerinfo() {
        return new Søkerinfo(person(), arbeidsforhold());
    }

    private static Person person() {
        return new Person(personDto());
    }

    public static PersonDto personDto() {
        PersonDto dto = new PersonDto();
        dto.fnr = "25987148243";
        dto.aktorId = "0123456789999";
        dto.navn = new Navn("SIGRID", null, "HOELSVEEN", Kjønn.K);
        dto.fødselsdato = now().minusYears(21);
        dto.landKode = NO;
        dto.bankkonto = new Bankkonto("1234567890", "Stub NOR");
        dto.barn = barn();
        return dto;
    }

    private static AnnenForelder annenForelder() {
        return new AnnenForelder("01017098765", new Navn("Steve", "Grønland", "Nichols", Kjønn.M),
                now().minusYears(45));
    }

    private static List<Barn> barn() {
        return List.of(
                new Barn("01011812345", new Navn("Mo", null, "Hoelsveen", Kjønn.M), now().minusYears(1), annenForelder()));
    }

    private static List<Arbeidsforhold> arbeidsforhold() {
        return List.of(new Arbeidsforhold("123456789", "orgnr", "KJELL T. RINGS SYKKELVERKSTED", 100d,
                now().minusYears(2), null));
    }

}
