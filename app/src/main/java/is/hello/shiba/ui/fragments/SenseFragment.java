package is.hello.shiba.ui.fragments;

import android.app.AlertDialog;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import javax.inject.Inject;

import is.hello.buruberi.bluetooth.devices.SensePeripheral;
import is.hello.buruberi.bluetooth.stacks.Peripheral;
import is.hello.shiba.R;
import is.hello.shiba.graph.SensePresenter;
import is.hello.shiba.graph.ShibaFragment;
import is.hello.shiba.ui.MainActivity;
import is.hello.shiba.ui.adapter.CommandAdapter;
import is.hello.shiba.ui.dialogs.ErrorDialogFragment;
import is.hello.shiba.ui.dialogs.LoadingDialogFragment;
import rx.Observable;
import rx.Subscription;

import static rx.android.observables.AndroidObservable.fromLocalBroadcast;

public class SenseFragment extends ShibaFragment {
    @Inject SensePresenter sense;

    private Toolbar toolbar;
    private ProgressBar activityIndicator;
    private ListView listView;
    private CommandAdapter adapter;

    private Subscription unexpectedDisconnects;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.adapter = new CommandAdapter(getActivity());

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sense, container, false);

        this.toolbar = (Toolbar) view.findViewById(R.id.fragment_sense_toolbar);
        toolbar.inflateMenu(R.menu.menu_sense);
        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_disconnect: {
                    stopListeningForDisconnects();
                    sense.disconnect().subscribe();
                    getFragmentManager().popBackStack();
                    return true;
                }

                default: {
                    return false;
                }
            }
        });

        this.activityIndicator = (ProgressBar) view.findViewById(R.id.fragment_sense_activity);
        this.listView = (ListView) view.findViewById(android.R.id.list);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(adapter);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activityIndicator.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        bind(sense.peripheral).subscribe(this::bindSense, this::senseUnavailable);
    }

    //region Bindings

    public void bindSense(@NonNull SensePeripheral sense) {
        toolbar.setSubtitle(sense.getDeviceId());
        showActions();

        activityIndicator.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);

        startListeningForDisconnects(sense);
    }

    public void senseUnavailable(Throwable e) {
        activityIndicator.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);

        stopListeningForDisconnects();
    }

    //endregion


    //region Connection Losses

    private void startListeningForDisconnects(@NonNull SensePeripheral sense) {
        stopListeningForDisconnects();

        IntentFilter disconnectFilter = new IntentFilter(Peripheral.ACTION_DISCONNECTED);
        Observable<SensePeripheral> disconnects = fromLocalBroadcast(getActivity().getApplicationContext(), disconnectFilter)
                .filter(intent -> {
                    String address = intent.getStringExtra(Peripheral.EXTRA_ADDRESS);
                    return TextUtils.equals(address, sense.getAddress());
                })
                .map(ignored -> sense);
        this.unexpectedDisconnects = bind(disconnects).subscribe(this::onSenseDisconnected);
    }

    private void stopListeningForDisconnects() {
        if (unexpectedDisconnects != null) {
            unexpectedDisconnects.unsubscribe();
            this.unexpectedDisconnects = null;
        }
    }

    public void onSenseDisconnected(@NonNull SensePeripheral sense) {
        clearActions();
    }

    //endregion


    //region Actions

    private void showActions() {
        adapter.addItem(R.string.action_put_into_normal_mode, this::putIntoNormalMode);
        adapter.addItem(R.string.action_put_into_pairing_mode, this::putIntoPairingMode);
        adapter.addItem(R.string.action_forget_phone, this::clearPairedPhone);
        adapter.addItem(R.string.action_factory_reset, this::factoryReset);
        adapter.addItem(R.string.action_get_wifi_network, this::getWifiNetwork);
        adapter.addItem(R.string.action_set_wifi_network, this::setWifiNetwork);
        adapter.addItem(R.string.action_pair_pill, this::pairPillMode);
        adapter.addItem(R.string.action_link_account, this::linkAccount);
        adapter.addItem(R.string.action_push_data, this::pushData);
        adapter.addItem(R.string.action_busy_led, this::busyLedAnimation);
        adapter.addItem(R.string.action_trippy_led, this::trippyLedAnimation);
        adapter.addItem(R.string.action_stop_led_animated, this::stopAnimationWithFade);
        adapter.addItem(R.string.action_stop_led, this::stopAnimationWithoutFade);
    }

    private void clearActions() {
        adapter.clear();
    }

    private <T> void doSimpleCommand(@NonNull Observable<T> command) {
        LoadingDialogFragment loadingDialogFragment = LoadingDialogFragment.show(getFragmentManager());
        bind(command).subscribe(ignored -> {
            loadingDialogFragment.dismiss();
        }, e -> {
            loadingDialogFragment.dismiss();
            ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(e);
            errorDialogFragment.show(getFragmentManager(), ErrorDialogFragment.TAG);
        });
    }

    public void putIntoNormalMode() {
        doSimpleCommand(sense.peripheral.flatMap(s -> s.setPairingModeEnabled(false)));
    }

    public void putIntoPairingMode() {
        doSimpleCommand(sense.peripheral.flatMap(s -> s.setPairingModeEnabled(true)));
    }

    public void clearPairedPhone() {
        doSimpleCommand(sense.peripheral.flatMap(SensePeripheral::clearPairedPhone));
    }

    public void factoryReset() {
        doSimpleCommand(sense.peripheral.flatMap(SensePeripheral::factoryReset));
    }

    public void getWifiNetwork() {
        Observable<SensePeripheral.SenseWifiNetwork> network = sense.peripheral.flatMap(SensePeripheral::getWifiNetwork);
        LoadingDialogFragment loadingDialogFragment = LoadingDialogFragment.show(getFragmentManager());
        bind(network).subscribe(connectedNetwork -> {
            loadingDialogFragment.dismiss();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.action_get_wifi_network);
            builder.setMessage(connectedNetwork.ssid + "\n" + connectedNetwork.connectionState);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.create().show();
        }, e -> {
            loadingDialogFragment.dismiss();
            ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(e);
            errorDialogFragment.show(getFragmentManager(), ErrorDialogFragment.TAG);
        });
    }

    public void setWifiNetwork() {
        ((MainActivity) getActivity()).pushFragment(new WiFiFragment());
    }

    public void pairPillMode() {
        doSimpleCommand(sense.pairPill());
    }

    public void linkAccount() {
        doSimpleCommand(sense.linkAccount());
    }

    public void pushData() {
        doSimpleCommand(sense.peripheral.flatMap(SensePeripheral::pushData));
    }

    public void busyLedAnimation() {
        doSimpleCommand(sense.peripheral.flatMap(s -> s.runLedAnimation(SensePeripheral.LedAnimation.BUSY)));
    }

    public void trippyLedAnimation() {
        doSimpleCommand(sense.peripheral.flatMap(s -> s.runLedAnimation(SensePeripheral.LedAnimation.TRIPPY)));
    }

    public void stopAnimationWithFade() {
        doSimpleCommand(sense.peripheral.flatMap(s -> s.runLedAnimation(SensePeripheral.LedAnimation.FADE_OUT)));
    }

    public void stopAnimationWithoutFade() {
        doSimpleCommand(sense.peripheral.flatMap(s -> s.runLedAnimation(SensePeripheral.LedAnimation.STOP)));
    }

    //endregion
}
