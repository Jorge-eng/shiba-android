package is.hello.shiba.testing;

import android.support.annotation.NonNull;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;

public class TestStep {
    private final String name;
    private final Observable<?> logic;
    private final long startDelay;


    private static final Random RANDOMIZER = new Random();
    public static long randomizeDelay(long delay, float maxPaddingFactor) {
        if (maxPaddingFactor == 1f) {
            return delay;
        } else {
            float factor = RANDOMIZER.nextFloat();
            float constrainedFactor = Math.min(maxPaddingFactor, factor);
            return Math.round(delay + (delay * constrainedFactor));
        }
    }

    public static TestStep immediate(@NonNull String name, @NonNull Observable<?> logic) {
        return new TestStep(name, logic, 0);
    }

    public static TestStep withDelay(@NonNull String name, @NonNull Observable<?> logic, long delay) {
        return new TestStep(name, logic, delay);
    }

    public static TestStep withRandomizedDelay(@NonNull String name,
                                               @NonNull Observable<?> logic,
                                               long baseDelay,
                                               float maxPaddingFactor) {
        return new TestStep(name, logic, randomizeDelay(baseDelay, maxPaddingFactor));
    }

    public TestStep(@NonNull String name,
                    @NonNull Observable<?> logic,
                    long startDelay) {
        this.name = name;
        this.logic = logic;
        this.startDelay = startDelay;
    }


    //region Properties

    public @NonNull String getName() {
        return name;
    }

    public @NonNull Observable<?> getLogic() {
        return logic;
    }

    public long getStartDelay() {
        return startDelay;
    }

    //endregion


    //region Identity

    @Override
    public String toString() {
        return "TestStep{" +
                "name='" + name + '\'' +
                ", logic=" + logic +
                ", startDelay=" + startDelay +
                '}';
    }

    //endregion


    //region Scheduling

    public Observable<?> runOn(@NonNull Scheduler scheduler) {
        return getLogic().delaySubscription(getStartDelay(), TimeUnit.MILLISECONDS, scheduler);
    }

    //endregion
}
