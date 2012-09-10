package name.alr.android_shopper;

import name.alr.android_shopper.util.WidgetUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * @author alopezruiz@gmail.com (Alejandro Lopez Ruiz)
 */
public class AddItemDialogManager {

    private EditText addItemEditText;

    private Button okButton;

    private AlertDialog dialog;

    public AddItemDialogManager(final Activity activity, final int dialogId,
            final DialogSubmitListener dialogSubmitListener) {
        AlertDialog.Builder addItemDialogBuilder = new AlertDialog.Builder(activity);
        addItemDialogBuilder.setTitle(activity.getString(R.string.add_item__title));
        View view = activity.getLayoutInflater().inflate(R.layout.add_item_dialog, null);
        addItemDialogBuilder.setView(view);

        this.addItemEditText = (EditText) view.findViewById(R.id.addItemEditText);

        this.addItemEditText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        String itemName = WidgetUtils.getTrimmedString(getAddItemEditText());
                        if (!itemName.isEmpty()) {
                            activity.dismissDialog(dialogId);
                            dialogSubmitListener.onSubmit(itemName);
                        } else {
                            getAddItemEditText().requestFocus();
                        }
                        return true;
                    }
                }
                return false;
            }

        });

        this.addItemEditText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // NOOP
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // NOOP
            }

            public void afterTextChanged(Editable editable) {
                AddItemDialogManager.this.okButton.setEnabled(!WidgetUtils.getTrimmedString(editable).isEmpty());
            }
        });

        addItemDialogBuilder.setPositiveButton(android.R.string.ok, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialogSubmitListener.onSubmit(WidgetUtils.getTrimmedString(getAddItemEditText()));
            }
        });
        addItemDialogBuilder.setNegativeButton(android.R.string.cancel, null);

        this.dialog = addItemDialogBuilder.create();
    }

    public Dialog getDialog() {
        return this.dialog;
    }

    public void onPrepareDialog(Bundle args) {
        if (this.okButton == null) {
            this.okButton = this.dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        }
        getAddItemEditText().setText("");
    }

    private EditText getAddItemEditText() {
        return this.addItemEditText;
    };

}
