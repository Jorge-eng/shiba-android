package is.hello.shiba.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import is.hello.shiba.R;
import is.hello.shiba.ui.adapter.SimpleSpinnerAdapter;
import is.hello.shiba.ui.util.EditorActionHandler;
import is.hello.shiba.ui.util.Styles;

import static is.hello.buruberi.bluetooth.devices.transmission.protobuf.SenseCommandProtos.wifi_endpoint.sec_type;

public class WiFiSignInDialogFragment extends DialogFragment implements TextWatcher, AdapterView.OnItemSelectedListener {
    public static final String TAG = WiFiSignInDialogFragment.class.getSimpleName();

    public static final String NETWORK = WiFiSignInDialogFragment.class.getName() + ".NETWORK";
    public static final String PASSWORD = WiFiSignInDialogFragment.class.getName() + ".PASSWORD";
    public static final String SEC_TYPE = WiFiSignInDialogFragment.class.getName() + ".SEC_TYPE";

    private Spinner securityType;
    private EditText network;
    private EditText password;
    private Button submitButton;


    public static WiFiSignInDialogFragment newInstance(@Nullable String network, @Nullable sec_type securityType) {
        WiFiSignInDialogFragment dialogFragment = new WiFiSignInDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putString(NETWORK, network);
        arguments.putSerializable(SEC_TYPE, securityType);
        dialogFragment.setArguments(arguments);

        return dialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_sign_into_wifi_network);

        LayoutInflater inflater = LayoutInflater.from(getActivity());

        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.fragment_dialog_wifi_sign_in, null);

        this.securityType = (Spinner) view.findViewById(R.id.fragment_dialog_wifi_sign_in_security);
        SimpleSpinnerAdapter<sec_type> adapter = new SimpleSpinnerAdapter<>(getActivity(), s -> getString(Styles.getSecTypeStringRes(s)));
        adapter.addAll(sec_type.values());
        securityType.setOnItemSelectedListener(this);
        securityType.setAdapter(adapter);

        this.network = (EditText) view.findViewById(R.id.fragment_dialog_wifi_sign_in_network);
        this.password = (EditText) view.findViewById(R.id.fragment_dialog_wifi_sign_in_pass);
        password.setOnEditorActionListener(new EditorActionHandler(this::submit));

        if (savedInstanceState == null) {
            securityType.setSelection(adapter.getPosition(getInitialSecurityType()));
            network.setText(getInitialNetwork());
            password.setText(getInitialPassword());
        }

        if (TextUtils.isEmpty(network.getText())) {
            network.requestFocus();
        }

        builder.setView(view);

        builder.setPositiveButton(R.string.action_sign_in, (dialog, which) -> submit());
        builder.setNegativeButton(android.R.string.cancel, null);

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (submitButton == null) {
            AlertDialog dialog = (AlertDialog) getDialog();
            this.submitButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            submitButton.setEnabled(false);

            network.addTextChangedListener(this);
            password.addTextChangedListener(this);
        }
    }

    private sec_type getInitialSecurityType() {
        if (getArguments().containsKey(SEC_TYPE)) {
            return (sec_type) getArguments().getSerializable(SEC_TYPE);
        } else {
            return sec_type.SL_SCAN_SEC_TYPE_WPA2;
        }
    }

    private String getInitialNetwork() {
        return getArguments().getString(NETWORK);
    }

    private String getInitialPassword() {
        return getArguments().getString(PASSWORD);
    }


    public void submit() {
        if (getTargetFragment() != null) {
            Intent response = new Intent();
            response.putExtra(SEC_TYPE, (sec_type) securityType.getSelectedItem());
            response.putExtra(NETWORK, network.getText().toString());
            response.putExtra(PASSWORD, password.getText().toString());
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, response);
        }
    }


    private void updateSubmitButton() {
        boolean hasNetworkName = !TextUtils.isEmpty(network.getText());
        boolean needsPassword = securityType.getSelectedItem() != sec_type.SL_SCAN_SEC_TYPE_OPEN;
        boolean hasPassword = !TextUtils.isEmpty(password.getText());
        submitButton.setEnabled(hasNetworkName && (!needsPassword || hasPassword));
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        updateSubmitButton();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        updateSubmitButton();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        updateSubmitButton();
    }
}
