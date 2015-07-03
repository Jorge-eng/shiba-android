package is.hello.shiba.graph;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

public class ShibaFragment extends Fragment {
    private static final Func1<ShibaFragment, Boolean> VALIDATOR = f -> f.isAdded() && !f.getActivity().isFinishing();

    public ShibaFragment() {
        ShibaApplication.getInstance().inject(this);
    }

    public boolean onBackPressed() {
        return false;
    }

    protected <T> Observable<T> bind(@NonNull Observable<T> observable) {
        return observable.subscribeOn(AndroidSchedulers.mainThread())
                         .lift(new OperatorConditionalBinding<>(this, VALIDATOR));
    }
}
