package is.hello.shiba.testing;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Iterator;
import java.util.List;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public final class IntegrationRun implements Observable.OnSubscribe<IntegrationEvent> {
    private final List<IntegrationStep> steps;

    //region Creation

    public static Observable<IntegrationEvent> create(@NonNull List<IntegrationStep> steps) {
        return Observable.create(new IntegrationRun(steps))
                         .observeOn(AndroidSchedulers.mainThread());
    }

    public IntegrationRun(@NonNull List<IntegrationStep> steps) {
        this.steps = steps;
    }

    //endregion


    //region Runs

    @Override
    public void call(Subscriber<? super IntegrationEvent> subscriber) {
        RunState state = new RunState(subscriber, Schedulers.computation(), steps.iterator());
        state.willStart();
        run(state);
    }

    private void run(@NonNull RunState state) {
        if (state.subscriber.isUnsubscribed()) {
            return;
        }

        if (!state.steps.hasNext()) {
            state.runCompleted();
            return;
        }

        IntegrationStep step = state.steps.next();
        step.runOn(state.scheduler)
            .doOnSubscribe(() -> state.stepWillRun(step))
            .subscribe(result -> {
                state.stepCompleted(step, result);
                run(state);
            }, e -> {
                state.stepFailed(step, e);
            });
    }

    //endregion


    private static final class RunState {
        private final Subscriber<? super IntegrationEvent> subscriber;
        private final Scheduler scheduler;
        private final Iterator<IntegrationStep> steps;

        private RunState(@NonNull Subscriber<? super IntegrationEvent> subscriber,
                         @NonNull Scheduler scheduler,
                         @NonNull Iterator<IntegrationStep> steps) {
            this.subscriber = subscriber;
            this.scheduler = scheduler;
            this.steps = steps;
        }

        private void willStart() {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(new IntegrationEvent(IntegrationEvent.Type.RUN_WILL_START));
            }
        }

        private void stepWillRun(@NonNull IntegrationStep step) {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(new IntegrationEvent(IntegrationEvent.Type.STEP_WILL_RUN, step));
            }
        }

        private void stepCompleted(@NonNull IntegrationStep step, @Nullable Object result) {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(new IntegrationEvent(IntegrationEvent.Type.STEP_COMPLETED, step, result));
            }
        }

        private void stepFailed(@NonNull IntegrationStep step, Throwable e) {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(new IntegrationEvent(IntegrationEvent.Type.STEP_FAILED, step, e));
            }
        }

        private void runCompleted() {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(new IntegrationEvent(IntegrationEvent.Type.RUN_COMPLETED));
                subscriber.onCompleted();
            }
        }
    }

}
