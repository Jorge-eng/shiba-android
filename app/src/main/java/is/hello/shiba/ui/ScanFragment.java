package is.hello.shiba.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import javax.inject.Inject;

import is.hello.buruberi.bluetooth.devices.SensePeripheral;
import is.hello.buruberi.bluetooth.stacks.BluetoothStack;
import is.hello.buruberi.bluetooth.stacks.util.PeripheralCriteria;
import is.hello.shiba.R;
import is.hello.shiba.graph.ShibaFragment;
import rx.Observable;
import rx.Subscription;

public class ScanFragment extends ShibaFragment implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    @Inject BluetoothStack stack;

    private SwipeRefreshLayout swipeRefreshLayout;
    private Subscription currentScan;
    private SimpleAdapter<SensePeripheral> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_refreshable, container, false);

        this.swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setOnItemClickListener(this);

        this.adapter = new SimpleAdapter<>(getActivity(), SensePeripheral::getName, SensePeripheral::getAddress);
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (currentScan == null && adapter.isEmpty()) {
            swipeRefreshLayout.setRefreshing(true);
            onRefresh();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }


    @Override
    public void onRefresh() {
        Observable<List<SensePeripheral>> peripherals = SensePeripheral.discover(stack, new PeripheralCriteria());
        this.currentScan = bind(peripherals).subscribe(this::bindPeripherals, this::scanFailed);
    }

    public void bindPeripherals(@NonNull List<SensePeripheral> peripherals) {
        swipeRefreshLayout.setRefreshing(false);

        adapter.clear();
        adapter.addAll(peripherals);
    }

    public void scanFailed(Throwable e) {
        swipeRefreshLayout.setRefreshing(false);

        adapter.clear();

        ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(e);
        errorDialogFragment.show(getFragmentManager(), ErrorDialogFragment.TAG);
    }
}
