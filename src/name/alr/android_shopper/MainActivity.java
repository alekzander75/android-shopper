package name.alr.android_shopper;

import name.alr.android_shopper.database.ShopItem;
import name.alr.android_shopper.database.ShopItemContentProvider;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Vibrator;
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

/**
 * @author alopezruiz@gmail.com (Alejandro Lopez Ruiz)
 */
public class MainActivity extends Activity {

    static final String SHOWING_ALL_BUNDLE_KEY = "showingAll";

    private static final boolean SHOWING_ALL_DEFAULT_VALUE = true;

    private final OnRemoveItemMenuItemClickListener onRemoveItemMenuItemClickListener = new OnRemoveItemMenuItemClickListener();
    private final AddItemDialogFragment.SubmitListener dialogSubmitListener = new AddItemDialogSubmitListener();

    private ListView listView;

    private boolean showingAll = SHOWING_ALL_DEFAULT_VALUE;

    private Intent preferencesIntent;

    private Vibrator vibrator;

    private MainListAdapter mainListAdapter;

    private LoaderCallbacks<Cursor> loaderCallbacks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        this.listView = (ListView) findViewById(R.id.mainListView);

        this.listView.setItemsCanFocus(false);
        this.listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        this.listView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
                AdapterContextMenuInfo adapterContextMenuInfo = (AdapterContextMenuInfo) menuInfo;
                Cursor item = getItem(adapterContextMenuInfo.id);
                String itemName = getItemName(item);
                item.close();

                menu.setHeaderTitle("\"" + itemName + "\"");
                MenuItem removeItemMenuItem = menu.add(0, R.id.remove_item_menu_item, Menu.NONE,
                        R.string.remove_item__title);
                removeItemMenuItem.setOnMenuItemClickListener(getOnRemoveItemMenuItemClickListener());
            }
        });

        mainListAdapter = new MainListAdapter(this, R.layout.main_list_entry, null, new String[] {
                ShopItem.AMOUNT_TO_BUY, ShopItem.NAME }, new int[] { R.id.mainListEntryAmountTextView,
                R.id.mainListEntryNameTextView }, this);
        this.listView.setAdapter(mainListAdapter);

        loaderCallbacks = new MainActityLoaderCallbacks(this, mainListAdapter);
        getLoaderManager().initLoader(0, newLoaderArgs(), loaderCallbacks);

        this.preferencesIntent = new Intent(this, ShopperPreferenceActivity.class);

        this.vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
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
            AddItemDialogFragment dialogFragment = new AddItemDialogFragment();
            dialogFragment.setSubmitListener(dialogSubmitListener);
            dialogFragment.show(getFragmentManager(), null);
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
        case (R.id.do_debug_action_menu_item): {
            doDebugAction();
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

    private void removeAllItems() {
        getContentResolver().delete(ShopItemContentProvider.ITEMS_CONTENT_URI, null, null);
    }

    private void removeItem(long id) {
        getContentResolver().delete(ShopItemContentProvider.getShopItemUri(id), null, null);
    }

    private void addItem(String name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ShopItem.NAME, name);
        getContentResolver().insert(ShopItemContentProvider.ITEMS_CONTENT_URI, contentValues);
    }

    private void doDebugAction() {
        // CHECK DATA
        // Cursor cursor = this.shopperOpenHelper.getItems();
        // try {
        // if (cursor.moveToFirst()) {
        // do {
        // long id = cursor.getLong(0);
        // int itemShopOrder = ShopperOpenHelper.getItemShopOrder(cursor);
        // String itemName = ShopperOpenHelper.getItemName(cursor);
        //
        // System.out.println(id + " " + itemShopOrder + " " + itemName);
        //
        // cursor.moveToNext();
        // } while (!cursor.isAfterLast());
        // }
        // } finally {
        // cursor.close();
        // }

        // EXPORT DATA
        // try {
        // FileOutputStream stream = openFileOutput("shopper-items.tsv", Context.MODE_WORLD_READABLE);
        // try {
        // Cursor cursor = this.shopperOpenHelper.getItems();
        // try {
        // if (cursor.moveToFirst()) {
        // do {
        // String line = ShopperOpenHelper.getItemShopOrder(cursor) + "\t"
        // + ShopperOpenHelper.getItemShopOrder(cursor) + "\t"
        // + ShopperOpenHelper.getItemName(cursor) + "\n";
        // stream.write(line.getBytes());
        // cursor.moveToNext();
        // } while (!cursor.isAfterLast());
        // }
        // } finally {
        // cursor.close();
        // }
        // } finally {
        // stream.close();
        // }
        // } catch (Exception exception) {
        // throw new RuntimeException("Failed to export items.", exception);
        // }
    }

    public void mainListEntryAlterButtonOnClick(View view) {
        if (this.isShowingAll()) {
            mainListEntryIncreaseButtonOnClick(view);
        } else {
            mainListEntryDecreaseButtonOnClick(view);
        }
    }

    private void mainListEntryIncreaseButtonOnClick(View view) {
        vibrateShort();

        long id = getItemId(view);
        getContentResolver().call(ShopItemContentProvider.ITEMS_CONTENT_URI,
                ShopItemContentProvider.INCREASE_ITEM_AMOUNT_METHOD, Long.toString(id), null);
    }

    /**
     * @return positioned {@link Cursor}, or <code>null</code>. remeber to {@link Cursor#close()} it.
     */
    private Cursor getItem(long id) {
        return getContentResolver().query(ShopItemContentProvider.getShopItemUri(id), null, null, null, null);
    }

    private static String getItemName(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(ShopItem.NAME));
    }

    private long getItemId(View view) {
        int position = this.listView.getPositionForView(view);
        return this.listView.getItemIdAtPosition(position);
    }

    private void mainListEntryDecreaseButtonOnClick(View view) {
        int position = this.listView.getPositionForView(view);
        Cursor item = (Cursor) this.listView.getItemAtPosition(position);
        int itemAmount = getItemAmount(item);

        if (itemAmount > 0) {
            if (itemAmount == 1) {
                vibrateShort();
            }
            getContentResolver().call(ShopItemContentProvider.ITEMS_CONTENT_URI,
                    ShopItemContentProvider.DECREASE_ITEM_AMOUNT_METHOD, Long.toString(item.getLong(0)), null);
        }
    }

    private static int getItemAmount(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(ShopItem.AMOUNT_TO_BUY));
    }

    public void mainListEntryRaiseButtonOnClick(View view) {
        int position = this.listView.getPositionForView(view);
        if (position > 0) {
            vibrateShort();
            long id = this.listView.getItemIdAtPosition(position);
            getContentResolver().call(ShopItemContentProvider.ITEMS_CONTENT_URI,
                    ShopItemContentProvider.RAISE_ITEM_METHOD, Long.toString(id), null);
        }
    }

    public void mainListEntryLowerButtonOnClick(View view) {
        int position = this.listView.getPositionForView(view);
        if (position < (this.listView.getCount() - 1)) {
            vibrateShort();
            long id = this.listView.getItemIdAtPosition(position);
            getContentResolver().call(ShopItemContentProvider.ITEMS_CONTENT_URI,
                    ShopItemContentProvider.LOWER_ITEM_METHOD, Long.toString(id), null);
        }
    }

    private void vibrateShort() {
        this.vibrator.vibrate(100);
    }

    private void toggleShownItems() {
        this.showingAll = !this.isShowingAll();
        getLoaderManager().restartLoader(0, newLoaderArgs(), this.loaderCallbacks);
    }

    private Bundle newLoaderArgs() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SHOWING_ALL_BUNDLE_KEY, showingAll);
        return bundle;
    }

    private OnRemoveItemMenuItemClickListener getOnRemoveItemMenuItemClickListener() {
        return this.onRemoveItemMenuItemClickListener;
    }

    boolean isShowingAll() {
        return this.showingAll;
    }

    private final class OnRemoveItemMenuItemClickListener implements OnMenuItemClickListener {
        public boolean onMenuItemClick(MenuItem item) {
            AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            removeItem(menuInfo.id);
            return true;
        }
    }

    private final class AddItemDialogSubmitListener implements AddItemDialogFragment.SubmitListener {
        public void onSubmit(String name) {
            addItem(name);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SHOWING_ALL_BUNDLE_KEY, showingAll);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }
        this.showingAll = savedInstanceState.getBoolean(SHOWING_ALL_BUNDLE_KEY, SHOWING_ALL_DEFAULT_VALUE);
    }

}
