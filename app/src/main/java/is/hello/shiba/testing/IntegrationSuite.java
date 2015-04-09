package is.hello.shiba.testing;

import android.util.SparseArray;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Subclasses should define a series of methods annotated with
 * {@link is.hello.shiba.testing.IntegrationSuite.Step} that
 * return {@link is.hello.shiba.testing.IntegrationStep}. The
 * returned steps will be run in the order in which they are
 * declared.
 */
public abstract class IntegrationSuite {
    /**
     * Finds and returns all of the step provider methods of the suite.
     */
    protected final SparseArray<Method> findStepProviders() {
        Class<Step> stepProviderClass = Step.class;
        Class<IntegrationStep> testStepClass = IntegrationStep.class;
        SparseArray<Method> stepProviderMethods = new SparseArray<>();
        for (Method method : getClass().getDeclaredMethods()) {
            Class<?> returnType = method.getReturnType();
            if (returnType.isAssignableFrom(testStepClass) &&
                    method.isAnnotationPresent(stepProviderClass)) {
                Step stepAnnotation = method.getAnnotation(stepProviderClass);
                stepProviderMethods.put(stepAnnotation.value(), method);
            }
        }
        return stepProviderMethods;
    }

    /**
     * Returns an observable that will find and return all of the
     * integration steps specified by the Step methods of the suite.
     */
    protected final Observable<List<IntegrationStep>> findTestSteps() {
        return Observable.create(s -> {
            SparseArray<Method> stepProviders = findStepProviders();
            List<IntegrationStep> steps = new ArrayList<>();
            for (int i = 0, size = stepProviders.size(); i < size; i++) {
                Method method = stepProviders.get(stepProviders.keyAt(i));
                method.setAccessible(true);
                try {
                    IntegrationStep step = (IntegrationStep) method.invoke(this, (Object[]) null);
                    steps.add(step);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    s.onError(e);
                    return;
                }
            }

            s.onNext(steps);
            s.onCompleted();
        });
    }

    /**
     * Creates a new integration test run.
     */
    public final Observable<IntegrationEvent> createRun() {
        return findTestSteps().flatMap(IntegrationRun::create);
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface Step {
        /**
         * Specifies the relative order of the step.
         * <p/>
         * Required because Android does not preserve method order.
         */
        int value();
    }
}
