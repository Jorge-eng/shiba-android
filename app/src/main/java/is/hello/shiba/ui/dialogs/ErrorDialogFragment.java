package is.hello.shiba.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import is.hello.buruberi.util.Errors;
import is.hello.shiba.R;

public class ErrorDialogFragment extends DialogFragment {
    public static final String TAG = ErrorDialogFragment.class.getSimpleName();

    private static final String ARG_MESSAGE = ErrorDialogFragment.class.getSimpleName() + ".ARG_MESSAGE";

    public static ErrorDialogFragment newInstance(Throwable e) {
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_MESSAGE, Errors.getDisplayMessage(e));
        dialogFragment.setArguments(bundle);

        return dialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_error);

        Errors.Message message = getArguments().getParcelable(ARG_MESSAGE);
        builder.setMessage(message.resolve(getActivity()));

        builder.setPositiveButton(android.R.string.ok, null);
        return builder.create();
    }
}
