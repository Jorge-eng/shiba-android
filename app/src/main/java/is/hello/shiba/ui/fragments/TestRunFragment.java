package is.hello.shiba.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import is.hello.shiba.R;
import is.hello.shiba.graph.ShibaFragment;
import is.hello.shiba.graph.presenters.SensePresenter;
import is.hello.shiba.testing.TestEventAdapter;
import is.hello.shiba.testing.TestRun;
import is.hello.shiba.testing.TestStep;
import rx.Observable;

public class TestRunFragment extends ShibaFragment {
    @Inject SensePresenter sense;

    private MenuItem toggleRunItem;
    private TestEventAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.adapter = new TestEventAdapter(getActivity());
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test_run, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.fragment_test_run_toolbar);
        toolbar.inflateMenu(R.menu.menu_test_run);
        this.toggleRunItem = toolbar.getMenu().findItem(R.id.action_toggle_run);
        toolbar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.action_toggle_run: {
                    toggleRun();
                    return true;
                }

                default: {
                    return false;
                }
            }
        });

        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setAdapter(adapter);

        return view;
    }


    public void toggleRun() {
        List<TestStep> steps = new ArrayList<>();

        steps.add(TestStep.immediate("Passing", Observable.just("Passed")));
        steps.add(TestStep.withDelay("Passing w/delay", Observable.just("Passed"), 1000));
        steps.add(TestStep.immediate("Failing", Observable.error(new Throwable("Shit's busted, yo"))));
        steps.add(TestStep.immediate("Unreachable", Observable.just("What now?")));

        Observable<TestRun.Event> testRun = TestRun.create(steps);
        adapter.attach(testRun);
    }
}
