package is.hello.shiba.ui.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import is.hello.shiba.R;

public class LoadingDialogFragment extends DialogFragment {
    public static final String TAG = LoadingDialogFragment.class.getSimpleName();

    public static LoadingDialogFragment show(@NonNull FragmentManager fm) {
        LoadingDialogFragment loadingDialogFragment = (LoadingDialogFragment) fm.findFragmentByTag(TAG);
        if (loadingDialogFragment == null) {
            loadingDialogFragment = new LoadingDialogFragment();
            loadingDialogFragment.show(fm, TAG);
        }

        return loadingDialogFragment;
    }

    public static void close(@NonNull FragmentManager fm) {
        LoadingDialogFragment loadingDialogFragment = (LoadingDialogFragment) fm.findFragmentByTag(TAG);
        if (loadingDialogFragment != null) {
            loadingDialogFragment.dismiss();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.title_loading));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }


    public void setMessage(@NonNull String message) {
        ((ProgressDialog) getDialog()).setMessage(message);
    }

    public void setMessage(@StringRes int messageRes) {
        setMessage(getString(messageRes));
    }
}
