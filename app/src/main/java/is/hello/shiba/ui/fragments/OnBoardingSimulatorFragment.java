package is.hello.shiba.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;

import java.util.EnumSet;

import javax.inject.Inject;

import is.hello.buruberi.bluetooth.devices.SensePeripheral;
import is.hello.buruberi.util.StringRef;
import is.hello.shiba.R;
import is.hello.shiba.graph.ShibaFragment;
import is.hello.shiba.graph.presenters.ApiPresenter;
import is.hello.shiba.graph.presenters.SensePresenter;
import is.hello.shiba.testing.IntegrationTestAdapter;
import is.hello.shiba.testing.suites.OnBoardingIntegrationSuite;
import is.hello.shiba.ui.dialogs.ErrorDialogFragment;
import is.hello.shiba.ui.dialogs.WiFiSignInDialogFragment;
import is.hello.shiba.ui.util.Optional;
import rx.Observable;

import static is.hello.buruberi.bluetooth.devices.transmission.protobuf.SenseCommandProtos.wifi_endpoint.sec_type;
import static is.hello.shiba.testing.suites.OnBoardingIntegrationSuite.Config;

public class OnBoardingSimulatorFragment extends ShibaFragment {
    private static final int REQUEST_SIGN_IN = 0x51;

    @Inject ApiPresenter api;
    @Inject SensePresenter sense;

    private CheckBox includeDelays;
    private CheckBox randomizeDelays;
    private IntegrationTestAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.adapter = new IntegrationTestAdapter(getActivity());
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test_run, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.fragment_test_run_toolbar);
        toolbar.inflateMenu(R.menu.menu_test_run);
        toolbar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.action_start: {
                    beginRun();
                    return true;
                }

                default: {
                    return false;
                }
            }
        });

        this.includeDelays = (CheckBox) view.findViewById(R.id.fragment_test_run_include_delays);
        this.randomizeDelays = (CheckBox) view.findViewById(R.id.fragment_test_run_randomize_delays);

        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setAdapter(adapter);

        return view;
    }


    public void beginRun() {
        WiFiSignInDialogFragment signInDialogFragment = WiFiSignInDialogFragment.newInstance(null, null);
        signInDialogFragment.setTargetFragment(this, REQUEST_SIGN_IN);
        signInDialogFragment.show(getFragmentManager(), WiFiSignInDialogFragment.TAG);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SIGN_IN && resultCode == Activity.RESULT_OK) {
            String network = data.getStringExtra(WiFiSignInDialogFragment.NETWORK);
            String password = data.getStringExtra(WiFiSignInDialogFragment.PASSWORD);
            sec_type securityType = (sec_type) data.getSerializableExtra(WiFiSignInDialogFragment.SEC_TYPE);
            createRunWith(network, password, securityType);
        }
    }

    public void createRunWith(@NonNull String network,
                              @Nullable String password,
                              @NonNull sec_type securityType) {
        Observable<Pair<Optional<String>, SensePeripheral>> values = Observable.combineLatest(api.accessToken, sense.peripheral, Pair::new);
        bind(values).subscribe(tokenAndSense -> {
            EnumSet<Config.Flag> flags = EnumSet.noneOf(Config.Flag.class);
            if (includeDelays.isChecked()) {
                flags.add(Config.Flag.INCLUDE_DELAYS);
            }
            if (randomizeDelays.isChecked()) {
                flags.add(Config.Flag.RANDOMIZE_DELAYS);
            }

            Optional<String> accessToken = tokenAndSense.first;
            if (!accessToken.isPresent()) {
                StringRef message = StringRef.from(R.string.message_access_token_required);
                ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(message);
                errorDialogFragment.show(getFragmentManager(), ErrorDialogFragment.TAG);
                return;
            }

            SensePeripheral peripheral = tokenAndSense.second;
            Config config = new Config(accessToken.get(), securityType, network, password, flags);
            OnBoardingIntegrationSuite suite = new OnBoardingIntegrationSuite(peripheral, config);
            adapter.run(suite.createRun());
        });
    }
}
