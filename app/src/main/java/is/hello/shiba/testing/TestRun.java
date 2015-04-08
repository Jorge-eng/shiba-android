package is.hello.shiba.testing;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import is.hello.shiba.ui.util.Optional;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public final class TestRun implements Observable.OnSubscribe<TestRun.Event> {
    private final List<TestStep> steps;

    //region Creation

    public static Observable<Event> create(@NonNull List<TestStep> steps) {
        return Observable.create(new TestRun(steps))
                         .observeOn(AndroidSchedulers.mainThread());
    }

    public TestRun(@NonNull List<TestStep> steps) {
        this.steps = steps;
    }

    //endregion


    //region Runs

    @Override
    public void call(Subscriber<? super Event> subscriber) {
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

        TestStep step = state.steps.next();
        step.runOn(state.scheduler)
            .doOnSubscribe(() -> state.stepWillRun(step))
            .subscribe(ignored -> {
                state.stepCompleted(step);
                run(state);
            }, e -> {
                state.stepFailed(step, e);
            });
    }

    //endregion


    private static final class RunState {
        private final Subscriber<? super Event> subscriber;
        private final Scheduler scheduler;
        private final Iterator<TestStep> steps;

        private RunState(@NonNull Subscriber<? super Event> subscriber,
                         @NonNull Scheduler scheduler,
                         @NonNull Iterator<TestStep> steps) {
            this.subscriber = subscriber;
            this.scheduler = scheduler;
            this.steps = steps;
        }

        private void willStart() {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(new Event(Event.Type.RUN_WILL_START, null, Optional.empty()));
            }
        }

        private void stepWillRun(@NonNull TestStep step) {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(new Event(Event.Type.STEP_WILL_RUN, step, Optional.empty()));
            }
        }

        private void stepCompleted(@NonNull TestStep step) {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(new Event(Event.Type.STEP_COMPLETED, step, Optional.empty()));
            }
        }

        private void stepFailed(@NonNull TestStep step, Throwable e) {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(new Event(Event.Type.STEP_FAILED, step, Optional.of(e)));
            }
        }

        private void runCompleted() {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(new Event(Event.Type.RUN_COMPLETED, null, Optional.empty()));
                subscriber.onCompleted();
            }
        }
    }

    public static final class Event {
        private final Type type;
        private final @Nullable TestStep step;
        private final Optional<Throwable> error;

        public Event(@NonNull Type type,
                     @Nullable TestStep step,
                     Optional<Throwable> error) {
            this.type = type;
            this.step = step;
            this.error = error;
        }


        //region Attributes

        public @NonNull Type getType() {
            return type;
        }

        public @Nullable TestStep getStep() {
            return step;
        }

        public Optional<Throwable> getError() {
            return error;
        }

        public CharSequence toMessage() {
            return getType().toMessage(getStep(), getError());
        }

        @Override
        public String toString() {
            return "TestEvent{" +
                    "type=" + type +
                    ", step=" + step +
                    ", error=" + error +
                    '}';
        }

        //endregion


        public static enum Type {
            RUN_WILL_START {
                @Override
                public CharSequence toMessage(@Nullable TestStep step,
                                              @Nullable Optional<Throwable> error) {
                    return "[Run starting]";
                }
            },
            RUN_FAILED {
                @Override
                public CharSequence toMessage(@Nullable TestStep step, @Nullable Optional<Throwable> error) {
                    String message = error.map(Throwable::getMessage).orElse("Unknown");
                    return formatAsError("[Run failed with error '" + message + "']");
                }
            },
            RUN_COMPLETED {
                @Override
                public CharSequence toMessage(@Nullable TestStep step, @Nullable Optional<Throwable> error) {
                    return "[Run completed]";
                }
            },
            STEP_WILL_RUN {
                @Override
                public CharSequence toMessage(@Nullable TestStep step,
                                              @Nullable Optional<Throwable> error) {
                    long delay = TimeUnit.MILLISECONDS.toSeconds(step.getStartDelay());
                    return "Starting step '" + step.getName() + "' (delay is " + delay + ")";
                }
            },
            STEP_COMPLETED {
                @Override
                public CharSequence toMessage(@Nullable TestStep step, @Nullable Optional<Throwable> error) {
                    return "Completed step '" + step.getName() + "'";
                }
            },
            STEP_FAILED {
                @Override
                public CharSequence toMessage(@Nullable TestStep step, @Nullable Optional<Throwable> error) {
                    String message = error.map(Throwable::getMessage).orElse("Unknown");
                    return formatAsError("Step '" + step.getName() + "' failed with error " + message);
                }
            };

            public abstract CharSequence toMessage(@Nullable TestStep step,
                                                   @Nullable Optional<Throwable> error);

            private static CharSequence formatAsError(@NonNull CharSequence rawMessage) {
                SpannableString message = new SpannableString(rawMessage);
                message.setSpan(new ForegroundColorSpan(Color.RED), 0, message.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                return message;
            }
        }
    }
}
