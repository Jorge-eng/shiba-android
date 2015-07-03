package is.hello.shiba.ui.fragments;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import is.hello.buruberi.bluetooth.devices.SensePeripheral;
import is.hello.shiba.R;
import is.hello.shiba.graph.ShibaFragment;
import is.hello.shiba.graph.presenters.SensePresenter;
import is.hello.shiba.ui.adapter.SimpleListAdapter;
import is.hello.shiba.ui.dialogs.ErrorDialogFragment;
import is.hello.shiba.ui.dialogs.LoadingDialogFragment;
import is.hello.shiba.ui.dialogs.WiFiSignInDialogFragment;
import is.hello.shiba.ui.util.Styles;
import rx.Observable;
import rx.Subscription;

import static is.hello.buruberi.bluetooth.devices.transmission.protobuf.SenseCommandProtos.wifi_endpoint;
import static is.hello.buruberi.bluetooth.devices.transmission.protobuf.SenseCommandProtos.wifi_endpoint.sec_type;

public class WiFiFragment extends ShibaFragment implements SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener {
    private static final int REQUEST_SIGN_IN = 0x51;

    @Inject SensePresenter sense;

    private SimpleListAdapter<wifi_endpoint> adapter;

    private ProgressBar activityIndicator;
    private SwipeRefreshLayout swipeRefreshLayout;

    private Subscription scanning;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.adapter = new SimpleListAdapter<>(getActivity(), wifi_endpoint::getSsid,
                e -> getString(Styles.getSecTypeStringRes(e.getSecurityType())));

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

        View otherItem = inflater.inflate(R.layout.item_simple_list, listView, false);

        TextView title = (TextView) otherItem.findViewById(R.id.item_detail_title);
        title.setText(R.string.title_other_network);

        otherItem.findViewById(R.id.item_detail_details)
                 .setVisibility(View.GONE);

        listView.addFooterView(otherItem);

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
        swipeRefreshLayout.setEnabled(false);

        adapter.clear();

        Observable<List<wifi_endpoint>> networks = sense.peripheral.flatMap(SensePeripheral::scanForWifiNetworks);
        this.scanning = bind(networks).subscribe(this::bindNetworks, this::networksUnavailable);
    }

    public void bindNetworks(@NonNull List<wifi_endpoint> networks) {
        activityIndicator.setVisibility(View.GONE);
        swipeRefreshLayout.setEnabled(true);
        this.scanning = null;

        adapter.addAll(networks);
    }

    public void networksUnavailable(Throwable e) {
        activityIndicator.setVisibility(View.GONE);
        swipeRefreshLayout.setEnabled(true);
        this.scanning = null;

        ErrorDialogFragment.presentError(getFragmentManager(), e);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SIGN_IN && resultCode == Activity.RESULT_OK) {
            String ssid = data.getStringExtra(WiFiSignInDialogFragment.NETWORK);
            String password = data.getStringExtra(WiFiSignInDialogFragment.PASSWORD);
            sec_type securityType = (sec_type) data.getSerializableExtra(WiFiSignInDialogFragment.SEC_TYPE);
            signIntoNetwork(ssid, password, securityType);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String ssid = null;
        sec_type securityType = null;

        wifi_endpoint endpoint = (wifi_endpoint) parent.getItemAtPosition(position);
        if (endpoint != null) {
            ssid = endpoint.getSsid();
            securityType = endpoint.getSecurityType();
        }

        WiFiSignInDialogFragment signInDialogFragment = WiFiSignInDialogFragment.newInstance(ssid, securityType);
        signInDialogFragment.setTargetFragment(this, REQUEST_SIGN_IN);
        signInDialogFragment.show(getFragmentManager(), WiFiSignInDialogFragment.TAG);
    }

    public void signIntoNetwork(@NonNull String ssid, @Nullable String password, @NonNull sec_type securityType) {
        Observable<Void> signIn = sense.peripheral.flatMap(p -> p.setWifiNetwork(ssid, securityType, password));

        LoadingDialogFragment.show(getFragmentManager());
        bind(signIn).subscribe(ignored -> {
            LoadingDialogFragment.close(getFragmentManager());
            getFragmentManager().popBackStack();
        }, e -> {
            LoadingDialogFragment.close(getFragmentManager());
            ErrorDialogFragment.presentError(getFragmentManager(), e);
        });
    }
}
