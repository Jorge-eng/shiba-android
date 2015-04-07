package is.hello.shiba.graph;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import rx.Observable;
import rx.android.observables.AndroidObservable;

public class ShibaFragment extends Fragment {
    public ShibaFragment() {
        ShibaApplication.getInstance().inject(this);
    }

    public boolean onBackPressed() {
        return false;
    }

    protected <T> Observable<T> bind(@NonNull Observable<T> observable) {
        return AndroidObservable.bindFragment(this, observable);
    }
}
