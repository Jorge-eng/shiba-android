package is.hello.shiba.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import javax.inject.Inject;

import is.hello.shiba.R;
import is.hello.shiba.api.ApiPresenter;
import is.hello.shiba.api.model.Environment;
import is.hello.shiba.api.model.OAuthCredentials;
import is.hello.shiba.graph.ShibaFragment;

public class SignInFragment extends ShibaFragment {
    @Inject ApiPresenter api;

    private Spinner environment;
    private TextView email;
    private TextView password;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);

        this.environment = (Spinner) view.findViewById(R.id.fragment_sign_in_environment);

        SimpleAdapter<Environment> adapter = new SimpleAdapter<>(getActivity(), Environment::getName, null);
        adapter.addAll(Environment.getAll());
        environment.setAdapter(adapter);

        this.email = (TextView) view.findViewById(R.id.fragment_sign_in_email);
        this.password = (TextView) view.findViewById(R.id.fragment_sign_in_password);
        password.setOnEditorActionListener(new EditorActionHandler(() -> submit(password)));

        Button submit = (Button) view.findViewById(R.id.fragment_sign_in_submit);
        submit.setOnClickListener(this::submit);

        return view;
    }


    public void submit(@NonNull View sender) {
        if (TextUtils.isEmpty(email.getText()) || TextUtils.isEmpty(password.getText())) {
            return;
        }

        Environment selectedEnvironment = (Environment) environment.getSelectedItem();
        OAuthCredentials credentials = new OAuthCredentials(selectedEnvironment,
                email.getText().toString(),
                password.getText().toString());

        LoadingDialogFragment.show(getFragmentManager());
        bind(api.service.flatMap(s -> s.authorize(credentials))).subscribe(session -> {
            LoadingDialogFragment.close(getFragmentManager());
            api.storeAccessToken(session.getAccessToken());
            getFragmentManager().popBackStack();
        }, e -> {
            LoadingDialogFragment.close(getFragmentManager());

            ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(e);
            errorDialogFragment.show(getFragmentManager(), ErrorDialogFragment.TAG);
        });
    }
}
