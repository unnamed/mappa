package team.unnamed.mappa.throwable;

public interface ThrowableBiConsumer<T, U> {

    void accept(T t, U u) throws Exception;
}
