package no.nav.foreldrepenger.selvbetjening.innsending.json;

public class Engangsstønad extends Søknad {

    public Barn barn;
    public AnnenForelder annenForelder;
    public Utenlandsopphold utenlandsopphold;

    public Engangsstønad() {
        this.barn = new Barn();
        this.utenlandsopphold = new Utenlandsopphold();
        this.annenForelder = new AnnenForelder();
    }
}
