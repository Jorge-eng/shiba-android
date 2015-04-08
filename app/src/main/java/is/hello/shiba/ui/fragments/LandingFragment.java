package is.hello.shiba.ui.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;

import javax.inject.Inject;

import is.hello.buruberi.bluetooth.stacks.BluetoothStack;
import is.hello.shiba.R;
import is.hello.shiba.graph.ShibaFragment;
import is.hello.shiba.graph.presenters.ApiPresenter;
import is.hello.shiba.logging.LogAdapter;
import is.hello.shiba.ui.MainActivity;

public class LandingFragment extends ShibaFragment implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {
    @Inject ApiPresenter api;
    @Inject BluetoothStack stack;

    private ListView logListView;
    private LogAdapter logAdapter;

    private TextView host;

    private Button sessionButton;
    private Button scanButton;

    //region Lifecycle

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landing, container, false);

        this.host = (TextView) view.findViewById(R.id.fragment_landing_host);

        TextView supportLevel = (TextView) view.findViewById(R.id.fragment_landing_support_level);
        supportLevel.setText(stack.getDeviceSupportLevel().toString());

        this.sessionButton = (Button) view.findViewById(R.id.fragment_landing_session);
        this.scanButton = (Button) view.findViewById(R.id.fragment_landing_scan);
        scanButton.setOnClickListener(this::scan);

        this.logListView = (ListView) view.findViewById(R.id.fragment_landing_log_list);
        logListView.setOnItemClickListener(this);
        logListView.setOnItemLongClickListener(this);
        this.logAdapter = new LogAdapter(getActivity());
        logListView.setAdapter(logAdapter);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bind(api.environment).subscribe(environment -> host.setText(environment.host));
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        logListView.setAdapter(null);
        this.logAdapter = null;
    }

    //endregion


    //region Actions

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

    //endregion


    //region Logs

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        DateTime time = logAdapter.getDate(position);
        Toast.makeText(getActivity().getApplicationContext(), time.toString("yyyy-MM-dd HH:dd"), Toast.LENGTH_SHORT).show();

        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DateTime time = logAdapter.getDate(position);
        String formattedMessage = logAdapter.getFormattedMessage(position);
        String messageWithTime = (time.toString() + " " + formattedMessage);

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, messageWithTime);
        startActivity(Intent.createChooser(share, "Share"));
    }

    //endregion
}
