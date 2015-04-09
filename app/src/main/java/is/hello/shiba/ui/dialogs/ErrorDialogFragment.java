package is.hello.shiba.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import is.hello.buruberi.util.Errors;
import is.hello.shiba.R;

public class ErrorDialogFragment extends DialogFragment {
    public static final String TAG = ErrorDialogFragment.class.getSimpleName();

    private static final String ARG_MESSAGE = ErrorDialogFragment.class.getSimpleName() + ".ARG_MESSAGE";

    //region Creation

    public static ErrorDialogFragment presentError(@NonNull FragmentManager fm, @Nullable Throwable e) {
        ErrorDialogFragment errorDialogFragment = newInstance(e);
        errorDialogFragment.show(fm, TAG);
        return errorDialogFragment;
    }

    public static ErrorDialogFragment newInstance(@Nullable Throwable e) {
        return newInstance(Errors.getDisplayMessage(e));
    }

    public static ErrorDialogFragment newInstance(@Nullable Errors.Message message) {
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_MESSAGE, message);
        dialogFragment.setArguments(bundle);

        return dialogFragment;
    }

    //endregion


    //region Lifecycle

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_error);

        Errors.Message message = getArguments().getParcelable(ARG_MESSAGE);
        if (message != null) {
            builder.setMessage(message.resolve(getActivity()));
        } else {
            builder.setMessage(R.string.message_generic_error);
        }

        builder.setPositiveButton(android.R.string.ok, null);
        return builder.create();
    }

    //endregion
}
