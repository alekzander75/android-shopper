package name.alr.android_shopper;

import name.alr.android_shopper.util.WidgetUtils;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;

/**
 * @author alopezruiz@gmail.com (Alejandro Lopez Ruiz)
 */
public class AddItemDialogFragment extends DialogFragment {

    public interface SubmitListener {
        void onSubmit(String name);
    }

    private SubmitListener submitListener;

    public AddItemDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder addItemDialogBuilder = new AlertDialog.Builder(getActivity());
        addItemDialogBuilder.setTitle(getActivity().getString(R.string.add_item__title));
        View view = getActivity().getLayoutInflater().inflate(R.layout.add_item_dialog, null);
        addItemDialogBuilder.setView(view);

        final EditText addItemEditText = (EditText) view.findViewById(R.id.addItemEditText);

        addItemEditText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        String itemName = WidgetUtils.getTrimmedString(addItemEditText);
                        if (!itemName.isEmpty()) {
                            submitListener.onSubmit(itemName);
                            AddItemDialogFragment.this.dismiss();
                        } else {
                            addItemEditText.requestFocus();
                        }
                        return true;
                    }
                }
                return false;
            }

        });

        addItemEditText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // NOOP
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // NOOP
            }

            public void afterTextChanged(Editable editable) {
                AlertDialog alertDialog = (AlertDialog) getDialog();
                Button button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                button.setEnabled(!WidgetUtils.getTrimmedString(editable).isEmpty());
            }
        });

        addItemDialogBuilder.setPositiveButton(android.R.string.ok, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                submitListener.onSubmit(WidgetUtils.getTrimmedString(addItemEditText));
            }
        });
        addItemDialogBuilder.setNegativeButton(android.R.string.cancel, null);

        AlertDialog alertDialog = addItemDialogBuilder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            public void onShow(DialogInterface dialog) {
                AlertDialog alertDialog = (AlertDialog) dialog;
                Button button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                button.setEnabled(false);
            }
        });

        return alertDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void setSubmitListener(SubmitListener submitListener) {
        this.submitListener = submitListener;
    }

}
