package name.alr.android_shopper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

import name.alr.android_shopper.database.ShopItem;
import name.alr.android_shopper.database.ShopItemContentProvider;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.Toast;

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
                MenuItem removeItemMenuItem = menu.add(0, Menu.NONE, Menu.NONE, R.string.remove_item__title);
                removeItemMenuItem.setOnMenuItemClickListener(getOnRemoveItemMenuItemClickListener());
            }
        });

        mainListAdapter = new MainListAdapter(this, R.layout.main_list_entry, null, new String[] {
                ShopItem.AMOUNT_TO_BUY, ShopItem.NAME }, new int[] { R.id.mainListEntryAmountTextView,
                R.id.mainListEntryNameTextView }, this);
        this.listView.setAdapter(mainListAdapter);

        loaderCallbacks = new MainActityLoaderCallbacks(this, mainListAdapter);
        getLoaderManager().initLoader(0, newLoaderArgs(), loaderCallbacks);

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
        case (R.id.export_items_menu_item): {
            exportItems();
            return true;
        }
        case (R.id.import_items_menu_item): {
            importItems();
            return true;
        }
        case (R.id.remove_all_items_menu_item): {
            // TODO: add confirm dialog
            // https://stackoverflow.com/questions/12912181/simplest-yes-no-dialog-fragment
            // TODO: re-enable after adding confirm dialog
            // removeAllItems();
            // Toast.makeText(this, R.string.remove_all_items__toast, Toast.LENGTH_SHORT).show();
            return true;
        }
        case (R.id.do_debug_action_menu_item): {
            doDebugAction();
            return true;
        }
        }

        return false;
    }

    private boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    private void exportItems() {
        if (!isExternalStorageWritable()) {
            Toast.makeText(this, R.string.export_items__device_busy_error, Toast.LENGTH_SHORT).show();
            return;
        }
        File directory = getExternalFilesDir(null);
        File file = new File(directory, "shopper-items.tsv");
        try {
            FileOutputStream stream = new FileOutputStream(file);
            try {
                Cursor cursor = this.getItems();
                try {
                    if (cursor.moveToFirst()) {
                        do {
                            String line = getItemAmount(cursor) + "\t" + getItemName(cursor) + "\n";
                            stream.write(line.getBytes());
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

        Toast.makeText(this, "Exported to " + file, Toast.LENGTH_LONG).show();
    }

    private void importItems() {
        if (!isExternalStorageReadable()) {
            Toast.makeText(this, R.string.import_items__device_busy_error, Toast.LENGTH_SHORT).show();
            return;
        }
        File directory = getExternalFilesDir(null);

        LinkedList<String> lines = new LinkedList<String>();
        try {
            File file = new File(directory, "shopper-items.tsv");
            FileInputStream stream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            } finally {
                reader.close();
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to read file.", exception);
        }

        // better safe than sorry
        if (!lines.isEmpty()) {
            removeAllItems();
        }

        for (String line : lines) {
            String[] split = line.split("\t");
            addItem(split[1], Integer.parseInt(split[0]));
        }

        Toast.makeText(this, R.string.import_items__toast, Toast.LENGTH_SHORT).show();
    }

    private void removeAllItems() {
        getContentResolver().delete(ShopItemContentProvider.ITEMS_CONTENT_URI, null, null);
    }

    private void removeItem(long id) {
        getContentResolver().delete(ShopItemContentProvider.getShopItemUri(id), null, null);
    }

    private void addItem(String name, int amount) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ShopItem.NAME, name);
        contentValues.put(ShopItem.AMOUNT_TO_BUY, amount);
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

    /**
     * @return ordered by shop order. remeber to {@link Cursor#close()} it.
     */
    private Cursor getItems() {
        return getContentResolver().query(ShopItemContentProvider.ITEMS_CONTENT_URI, null, null, null,
                ShopItem.SHOP_ORDER);
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
            addItem(name, 1);
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
