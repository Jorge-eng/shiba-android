package is.hello.shiba.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.List;

import javax.inject.Inject;

import is.hello.buruberi.bluetooth.devices.SensePeripheral;
import is.hello.buruberi.bluetooth.stacks.BluetoothStack;
import is.hello.buruberi.bluetooth.stacks.util.PeripheralCriteria;
import is.hello.shiba.R;
import is.hello.shiba.graph.ShibaFragment;
import rx.Observable;
import rx.Subscription;

public class ScanFragment extends ShibaFragment implements AdapterView.OnItemClickListener {
    @Inject BluetoothStack stack;

    private ProgressBar activityIndicator;
    private MenuItem scanItem;
    private Subscription currentScan;
    private SimpleAdapter<SensePeripheral> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        this.activityIndicator = (ProgressBar) view.findViewById(R.id.fragment_scan_activity);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.fragment_scan_toolbar);
        toolbar.inflateMenu(R.menu.menu_scan);
        this.scanItem = toolbar.getMenu().findItem(R.id.action_scan);
        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_scan: {
                    beginScan();
                    return true;
                }

                default: {
                    return false;
                }
            }
        });

        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setOnItemClickListener(this);

        this.adapter = new SimpleAdapter<>(getActivity(), SensePeripheral::getName, SensePeripheral::getDeviceId);
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (currentScan == null && adapter.isEmpty()) {
            beginScan();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }


    public void beginScan() {
        scanItem.setEnabled(false);
        activityIndicator.setVisibility(View.VISIBLE);

        adapter.clear();

        Observable<List<SensePeripheral>> peripherals = SensePeripheral.discover(stack, new PeripheralCriteria());
        this.currentScan = bind(peripherals).subscribe(this::bindPeripherals, this::scanFailed);
    }

    public void bindPeripherals(@NonNull List<SensePeripheral> peripherals) {
        scanItem.setEnabled(true);
        activityIndicator.setVisibility(View.GONE);

        adapter.clear();
        adapter.addAll(peripherals);
    }

    public void scanFailed(Throwable e) {
        scanItem.setEnabled(true);
        activityIndicator.setVisibility(View.GONE);

        ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(e);
        errorDialogFragment.show(getFragmentManager(), ErrorDialogFragment.TAG);
    }
}
