package no.nav.foreldrepenger.selvbetjening.innsending.tjeneste.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import no.nav.foreldrepenger.selvbetjening.innsending.json.Vedlegg;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@JsonInclude(NON_EMPTY)
public class SøknadDto {

    public LocalDateTime mottattdato;
    public SøkerDto søker;
    public List<VedleggDto> vedlegg = new ArrayList<>();

    public void addVedlegg(Vedlegg vedlegg) {
        VedleggDto vedleggDto = new VedleggDto();
        vedleggDto.type = "påkrevd";
        vedleggDto.metadata.id = vedlegg.id;
        vedleggDto.metadata.beskrivelse = vedlegg.type;
        vedleggDto.metadata.dokumentType = vedlegg.skjemanummer;
        vedleggDto.vedlegg = vedlegg.content;
        this.vedlegg.add(vedleggDto);
    }
}
