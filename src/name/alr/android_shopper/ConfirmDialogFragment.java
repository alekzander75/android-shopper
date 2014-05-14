package name.alr.android_shopper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

/**
 * @author alopezruiz@gmail.com (Alejandro Lopez Ruiz)
 */
public class ConfirmDialogFragment extends DialogFragment {

    public interface Listener {
        void onConfirm();
    }

    private static final String TITLE_RES_ID = "titleResId";

    private Listener listener;

    public static ConfirmDialogFragment newInstance(int titleResId, Listener listener) {
        ConfirmDialogFragment frag = new ConfirmDialogFragment();
        frag.listener = listener;

        Bundle args = new Bundle();
        args.putInt(TITLE_RES_ID, titleResId);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int titleResId = getArguments().getInt(TITLE_RES_ID);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(getActivity().getString(titleResId));
        dialogBuilder.setMessage(getActivity().getString(R.string.confirm_dialog__message));

        dialogBuilder.setPositiveButton(android.R.string.yes, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                listener.onConfirm();
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.no, null);

        return dialogBuilder.create();
    }

}
