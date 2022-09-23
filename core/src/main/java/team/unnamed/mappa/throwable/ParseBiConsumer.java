package team.unnamed.mappa.throwable;

public interface ParseBiConsumer<T, U> extends ThrowableBiConsumer<T, U> {

    @Override
    void accept(T t, U u) throws ParseException;
}
