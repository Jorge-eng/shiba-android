package is.hello.shiba.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import javax.inject.Inject;

import is.hello.buruberi.bluetooth.stacks.BluetoothStack;
import is.hello.shiba.R;
import is.hello.shiba.api.ApiPresenter;
import is.hello.shiba.graph.ShibaFragment;

public class LandingFragment extends ShibaFragment {
    @Inject ApiPresenter api;
    @Inject BluetoothStack stack;

    private Button sessionButton;
    private Button scanButton;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landing, container, false);

        this.sessionButton = (Button) view.findViewById(R.id.fragment_landing_session);

        this.scanButton = (Button) view.findViewById(R.id.fragment_landing_scan);
        scanButton.setOnClickListener(this::scan);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bind(api.accessToken).subscribe(accessToken -> {
            if (accessToken != null) {
                sessionButton.setOnClickListener(this::signOut);
                sessionButton.setText(R.string.action_sign_out);
            } else {
                sessionButton.setOnClickListener(this::signIn);
                sessionButton.setText(R.string.action_sign_in);
            }
        });

        bind(stack.isEnabled()).subscribe(scanButton::setEnabled);
    }

    public void signIn(@NonNull View sender) {
        ((MainActivity) getActivity()).pushFragment(new SignInFragment());
    }

    public void signOut(@NonNull View sender) {
        AlertDialog.Builder confirmation = new AlertDialog.Builder(getActivity());
        confirmation.setTitle(R.string.action_sign_out);
        confirmation.setMessage(R.string.message_confirm_sign_out);
        confirmation.setPositiveButton(R.string.action_sign_out, (dialog, which) -> api.clearAccessToken());
        confirmation.setNegativeButton(android.R.string.cancel, null);
        confirmation.create().show();
    }

    public void scan(@NonNull View sender) {
        ((MainActivity) getActivity()).pushFragment(new ScanFragment());
    }
}
