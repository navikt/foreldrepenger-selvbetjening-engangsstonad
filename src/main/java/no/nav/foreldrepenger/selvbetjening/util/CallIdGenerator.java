package no.nav.foreldrepenger.selvbetjening.util;

public interface CallIdGenerator {

    String getOrCreate();

    String create();

    String getKey();

}
