package is.hello.shiba.ui.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;

public final class Optional<T> {
    private static final Optional<?> EMPTY_INSTANCE = new Optional<>(null);
    private final @Nullable T value;

    //region Creation

    public static <T> Optional<T> empty() {
        //noinspection unchecked
        return (Optional<T>) EMPTY_INSTANCE;
    }

    public static <T> Optional<T> of(@NonNull T value) {
        //noinspection ConstantConditions
        if (value == null) {
            throw new NullPointerException("value cannot be null");
        }

        return new Optional<>(value);
    }

    public static <T> Optional<T> ofNullable(@Nullable T value) {
        return new Optional<>(value);
    }

    private Optional(@Nullable T value) {
        this.value = value;
    }

    //endregion


    //region Primitive Methods

    public boolean isPresent() {
        return (value != null);
    }

    public @NonNull T get() {
        if (value == null) {
            throw new NullPointerException();
        }

        return value;
    }

    //endregion


    //region Identity

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Optional)) {
            return false;
        }

        //noinspection unchecked
        Optional<T> that = (Optional<T>) o;
        return ((isPresent() == that.isPresent()) &&
                (!isPresent() || get().equals(that.get())));
    }

    @Override
    public int hashCode() {
        if (isPresent()) {
            return get().hashCode();
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        if (isPresent()) {
            return "{Optional of=" + get() + "}";
        } else {
            return "{Optional empty}";
        }
    }

    //endregion


    //region Operations

    public Optional<T> filter(@NonNull Func1<? super T, Boolean> predicate) {
        if (isPresent() && predicate.call(get())) {
            return this;
        } else {
            return empty();
        }
    }

    public <U> Optional<U> map(@NonNull Func1<? super T, ? extends U> mapper) {
        if (isPresent()) {
            return Optional.of(mapper.call(get()));
        } else {
            return empty();
        }
    }

    public <U> Optional<U> flatMap(@NonNull Func1<? super T, Optional<U>> mapper) {
        if (isPresent()) {
            return mapper.call(get());
        } else {
            return empty();
        }
    }

    public void ifPresent(@NonNull Action1<? super T> consumer) {
        if (isPresent()) {
            consumer.call(get());
        }
    }

    //endregion


    //region Or else

    public @NonNull T orElse(@NonNull T value) {
        if (isPresent()) {
            return get();
        } else {
            return value;
        }
    }

    public @NonNull T orElseGet(@NonNull Func0<T> supplier) {
        if (isPresent()) {
            return get();
        } else {
            return supplier.call();
        }
    }

    public @NonNull <X extends Throwable> T orElseThrow(@NonNull Func0<X> supplier) throws X {
        if (isPresent()) {
            return get();
        } else {
            throw supplier.call();
        }
    }

    //endregion
}
