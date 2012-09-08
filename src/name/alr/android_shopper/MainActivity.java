package name.alr.android_shopper;

import name.alr.android_shopper.database.ShopItem;
import name.alr.android_shopper.database.ShopperOpenHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * @author alopezruiz@gmail.com (Alejandro Lopez Ruiz)
 */
public class MainActivity extends Activity {

    private static final int ADD_ITEM_DIALOG_ID = 1;

    private ShopperOpenHelper shopperOpenHelper;

    private ListView listView;
    private BaseAdapter listAdapter;
    private Cursor itemsCursor;
    private EditText addItemEditText;
    private Button okButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        this.shopperOpenHelper = new ShopperOpenHelper(this);
        this.shopperOpenHelper.initialize();

        this.itemsCursor = this.shopperOpenHelper.getItems();

        startManagingCursor(this.itemsCursor);

        this.listView = (ListView) findViewById(R.id.mainListView);

        this.listAdapter = new SimpleCursorAdapter(this, R.layout.main_list_entry, this.itemsCursor,
                new String[] { ShopItem.NAME }, new int[] { R.id.mainListEntryText });
        this.listView.setAdapter(this.listAdapter);

        this.listView.setItemsCanFocus(false);
        this.listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        registerForContextMenu(this.listView);
    }

    @Override
    protected void onDestroy() {
        this.shopperOpenHelper.closeDatabase();

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
        case (R.id.add_item_menu_item): {
            showDialog(ADD_ITEM_DIALOG_ID);
            return true;
        }
        case (R.id.remove_item_menu_item): {
            removeItem(this.listView.getSelectedItemId());
            return true;
        }
        case (R.id.remove_all_items_menu_item): {
            removeAllItems();
            return true;
        }
        case (R.id.preferences_menu_item): {
            startActivity(new Intent(this, ShopperPreferenceActivity.class));
            return true;
        }
        }

        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        int idx = this.listView.getSelectedItemPosition();

        String removeTitle = getString(R.string.remove);

        MenuItem removeItem = menu.findItem(R.id.remove_item_menu_item);
        removeItem.setTitle(removeTitle);
        removeItem.setVisible(idx > -1);

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.setHeaderTitle("Selected Item");
        MenuItem removeItemMenuItem = menu.add(0, R.id.remove_item_menu_item, Menu.NONE, R.string.remove);
        removeItemMenuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                removeItem(menuInfo.id);
                return true;
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
        case ADD_ITEM_DIALOG_ID:
            AlertDialog.Builder addItemDialogBuilder = new AlertDialog.Builder(this);
            addItemDialogBuilder.setIcon(R.drawable.add_new_item);
            addItemDialogBuilder.setTitle(getString(R.string.add_new_item));
            View view = getLayoutInflater().inflate(R.layout.add_item_dialog, null);
            addItemDialogBuilder.setView(view);

            this.addItemEditText = (EditText) view.findViewById(R.id.addItemEditText);

            this.addItemEditText.setOnKeyListener(new OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (keyCode == KeyEvent.KEYCODE_ENTER) {
                            String itemName = getTrimmedString(getAddItemEditText());
                            if (!itemName.isEmpty()) {
                                dismissDialog(ADD_ITEM_DIALOG_ID);
                                addItem(itemName);
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
                    MainActivity.this.okButton.setEnabled(!getTrimmedString(editable).isEmpty());
                }
            });

            addItemDialogBuilder.setPositiveButton(android.R.string.ok, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    addItem(getTrimmedString(getAddItemEditText()));
                }
            });
            addItemDialogBuilder.setNegativeButton(android.R.string.cancel, null);

            return addItemDialogBuilder.create();
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        switch (id) {
        case ADD_ITEM_DIALOG_ID:
            AlertDialog addItemDialog = (AlertDialog) dialog;
            if (this.okButton == null) {
                this.okButton = addItemDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            }
            getAddItemEditText().setText("");

            break;
        }
    }

    private void removeAllItems() {
        this.shopperOpenHelper.deleteAllItems();
        this.itemsCursor.requery();
    }

    private void removeItem(long id) {
        this.shopperOpenHelper.deleteItem(id);
        this.itemsCursor.requery();
    }

    private void addItem(String name) {
        this.shopperOpenHelper.addItem(name);
        this.itemsCursor.requery();
    }

    private EditText getAddItemEditText() {
        return this.addItemEditText;
    };

    private String getTrimmedString(EditText editText) {
        return getTrimmedString(editText.getText());
    }

    private String getTrimmedString(Editable editable) {
        return editable.toString().trim();
    }

}
