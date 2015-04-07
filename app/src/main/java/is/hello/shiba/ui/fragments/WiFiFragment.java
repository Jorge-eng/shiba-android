package is.hello.shiba.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.List;

import javax.inject.Inject;

import is.hello.buruberi.bluetooth.devices.SensePeripheral;
import is.hello.shiba.R;
import is.hello.shiba.graph.SensePresenter;
import is.hello.shiba.graph.ShibaFragment;
import is.hello.shiba.ui.adapter.SimpleAdapter;
import is.hello.shiba.ui.dialogs.ErrorDialogFragment;
import is.hello.shiba.ui.util.Styles;
import rx.Observable;
import rx.Subscription;

import static is.hello.buruberi.bluetooth.devices.transmission.protobuf.SenseCommandProtos.wifi_endpoint;

public class WiFiFragment extends ShibaFragment implements SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener {
    @Inject SensePresenter sense;

    private SimpleAdapter<wifi_endpoint> adapter;

    private ProgressBar activityIndicator;
    private SwipeRefreshLayout swipeRefreshLayout;

    private Subscription scanning;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.adapter = new SimpleAdapter<>(getActivity(), wifi_endpoint::getSsid, e -> getString(Styles.getSecTypeStringRes(e.getSecurityType())));

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wifi, container, false);

        this.activityIndicator = (ProgressBar) view.findViewById(R.id.fragment_wifi_activity);
        this.swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_wifi_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setOnItemClickListener(this);
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (scanning == null && adapter.isEmpty()) {
            scanForNetworks();
        }
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(false);
        scanForNetworks();
    }


    public void scanForNetworks() {
        if (scanning != null) {
            return;
        }

        activityIndicator.setVisibility(View.VISIBLE);

        adapter.clear();

        Observable<List<wifi_endpoint>> networks = sense.peripheral.flatMap(SensePeripheral::scanForWifiNetworks);
        this.scanning = bind(networks).subscribe(this::bindNetworks, this::networksUnavailable);
    }

    public void bindNetworks(@NonNull List<wifi_endpoint> networks) {
        activityIndicator.setVisibility(View.GONE);
        this.scanning = null;

        adapter.addAll(networks);
    }

    public void networksUnavailable(Throwable e) {
        activityIndicator.setVisibility(View.GONE);
        this.scanning = null;

        ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(e);
        errorDialogFragment.show(getFragmentManager(), ErrorDialogFragment.TAG);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
