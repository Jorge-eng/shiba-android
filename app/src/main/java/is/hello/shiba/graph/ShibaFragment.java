package is.hello.shiba.graph;

import android.support.v4.app.Fragment;

public class ShibaFragment extends Fragment {
    public ShibaFragment() {
        ShibaApplication.getInstance().inject(this);
    }
}
