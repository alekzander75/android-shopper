package name.alr.android_shopper;

import java.io.FileOutputStream;

import name.alr.android_shopper.database.ShopItem;
import name.alr.android_shopper.database.ShopperOpenHelper;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * @author alopezruiz@gmail.com (Alejandro Lopez Ruiz)
 */
public class MainActivity extends Activity {

    private static final int ADD_ITEM_DIALOG_ID = 1;

    private ShopperOpenHelper shopperOpenHelper;

    private ListView listView;

    private Cursor itemsCursor;

    private AddItemDialogManager addItemDialogManager;

    private boolean showingAll = false;

    private Intent preferencesIntent;

    private final OnRemoveItemMenuItemClickListener onRemoveItemMenuItemClickListener = new OnRemoveItemMenuItemClickListener();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        this.shopperOpenHelper = new ShopperOpenHelper(this);
        this.shopperOpenHelper.initialize();

        setupItemsCursor();

        this.listView = (ListView) findViewById(R.id.mainListView);

        setupMainListAdapter();

        this.listView.setItemsCanFocus(false);
        this.listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        this.listView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
                AdapterContextMenuInfo adapterContextMenuInfo = (AdapterContextMenuInfo) menuInfo;
                Cursor item = getShopperOpenHelper().getItem(adapterContextMenuInfo.id);
                String itemName = ShopperOpenHelper.getItemName(item);
                item.close();

                menu.setHeaderTitle("\"" + itemName + "\"");
                MenuItem removeItemMenuItem = menu.add(0, R.id.remove_item_menu_item, Menu.NONE,
                        R.string.remove_item__title);
                removeItemMenuItem.setOnMenuItemClickListener(getOnRemoveItemMenuItemClickListener());
            }
        });

        this.preferencesIntent = new Intent(this, ShopperPreferenceActivity.class);
    }

    @Override
    protected void onDestroy() {
        this.shopperOpenHelper.closeDatabase();

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
        case (R.id.toggle_shown_menu_item): {
            toggleShownItems();
            return true;
        }
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
        case (R.id.export_items_menu_item): {
            exportItems();
            return true;
        }
        case (R.id.preferences_menu_item): {
            startActivity(this.preferencesIntent);
            return true;
        }
        }

        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem removeItem = menu.findItem(R.id.remove_item_menu_item);
        removeItem.setVisible(this.listView.getSelectedItemPosition() > -1);

        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
        case ADD_ITEM_DIALOG_ID:
            this.addItemDialogManager = new AddItemDialogManager(this, ADD_ITEM_DIALOG_ID, new DialogSubmitListener() {
                public void onSubmit(String name) {
                    addItem(name);
                }
            });
            return this.addItemDialogManager.getDialog();
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        switch (id) {
        case ADD_ITEM_DIALOG_ID:
            this.addItemDialogManager.onPrepareDialog();
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

    @SuppressLint("WorldReadableFiles")
    private void exportItems() {
        try {
            FileOutputStream stream = openFileOutput("shopper-items.tsv", Context.MODE_WORLD_READABLE);
            try {
                Cursor cursor = this.shopperOpenHelper.getItems();
                try {
                    if (cursor.moveToFirst()) {
                        do {
                            stream.write((ShopperOpenHelper.getItemShopOrder(cursor) + "\t"
                                    + ShopperOpenHelper.getItemShopOrder(cursor) + "\t"
                                    + ShopperOpenHelper.getItemName(cursor) + "\n").getBytes());
                            cursor.moveToNext();
                        } while (!cursor.isAfterLast());
                    }
                } finally {
                    cursor.close();
                }
            } finally {
                stream.close();
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to export items.", exception);
        }
    }

    public void mainListEntryIncreaseButtonOnClick(View view) {
        this.shopperOpenHelper.increaseItemAmount(getItemId(view));
        this.itemsCursor.requery();
    }

    private long getItemId(View view) {
        int position = this.listView.getPositionForView(view);
        return this.listView.getItemIdAtPosition(position);
    }

    public void mainListEntryDecreaseButtonOnClick(View view) {
        this.shopperOpenHelper.decreaseItemAmount(getItemId(view));
        this.itemsCursor.requery();
    }

    public void mainListEntryRaiseButtonOnClick(View view) {
        int position = this.listView.getPositionForView(view);
        if (position > 0) {
            this.shopperOpenHelper.raiseItem(this.listView.getItemIdAtPosition(position));
            this.itemsCursor.requery();
        }
    }

    public void mainListEntryLowerButtonOnClick(View view) {
        int position = this.listView.getPositionForView(view);
        if (position < (this.listView.getCount() - 1)) {
            this.shopperOpenHelper.lowerItem(getItemId(view));
            this.itemsCursor.requery();
        }
    }

    private void toggleShownItems() {
        stopManagingCursor(this.itemsCursor);
        this.itemsCursor.close();
        this.showingAll = !this.showingAll;
        setupItemsCursor();
        setupMainListAdapter();
    }

    private void setupItemsCursor() {
        this.itemsCursor = this.shopperOpenHelper.getListItems(this.showingAll);
        startManagingCursor(this.itemsCursor);
    }

    /**
     * Uses current {@link #listView} and {@link #itemsCursor}.
     */
    private void setupMainListAdapter() {
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.main_list_entry, this.itemsCursor,
                new String[] { ShopItem.AMOUNT_TO_BUY, ShopItem.NAME }, new int[] { R.id.mainListEntryAmountTextView,
                R.id.mainListEntryNameTextView });
        this.listView.setAdapter(adapter);
    }
    
    private OnRemoveItemMenuItemClickListener getOnRemoveItemMenuItemClickListener() {
        return this.onRemoveItemMenuItemClickListener;
    }

    private ShopperOpenHelper getShopperOpenHelper() {
        return this.shopperOpenHelper;
    }

    private final class OnRemoveItemMenuItemClickListener implements OnMenuItemClickListener {
        public boolean onMenuItemClick(MenuItem item) {
            AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            removeItem(menuInfo.id);
            return true;
        }
    }

}
