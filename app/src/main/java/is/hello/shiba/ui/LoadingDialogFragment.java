package is.hello.shiba.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import is.hello.shiba.R;

public class LoadingDialogFragment extends DialogFragment {
    public static final String TAG = LoadingDialogFragment.class.getSimpleName();

    public static void show(@NonNull FragmentManager fm) {
        if (fm.findFragmentByTag(TAG) == null) {
            LoadingDialogFragment loadingDialogFragment = new LoadingDialogFragment();
            loadingDialogFragment.show(fm, TAG);
        }
    }

    public static void close(@NonNull FragmentManager fm) {
        LoadingDialogFragment loadingDialogFragment = (LoadingDialogFragment) fm.findFragmentByTag(TAG);
        if (loadingDialogFragment != null) {
            loadingDialogFragment.dismiss();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.title_loading));
        dialog.setIndeterminate(true);
        return dialog;
    }
}
