package name.alr.android_shopper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import name.alr.android_shopper.database.ShopItem;
import name.alr.android_shopper.database.ShopItemContentProvider;
import name.alr.android_shopper.util.IoUtils;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.content.Intent;
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

import com.lamerman.FileDialog;
import com.lamerman.SelectionMode;

/**
 * @author alopezruiz@gmail.com (Alejandro Lopez Ruiz)
 */
public class MainActivity extends Activity {

    static final String SHOWING_ALL_BUNDLE_KEY = "showingAll";

    private static final String FILE_EXTENSION = "tsv";
    private static final boolean SHOWING_ALL_DEFAULT_VALUE = true;
    private static final int FILE_IMPORT_REQUEST_CODE = 1;

    // private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private final OnRemoveItemMenuItemClickListener onRemoveItemMenuItemClickListener = new OnRemoveItemMenuItemClickListener();
    private final AddItemDialogFragment.SubmitListener addItemListener = new AddItemDialogSubmitListener();
    private final ConfirmDialogFragment.Listener confirmListener = new ConfirmDialogListener();

    private ListView listView;

    private boolean showingAll = SHOWING_ALL_DEFAULT_VALUE;

    private Vibrator vibrator;

    private MainListAdapter mainListAdapter;

    private LoaderCallbacks<Cursor> loaderCallbacks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Log.i(LOG_TAG, "Creating main activity. showingAll=" + showingAll);
        if (savedInstanceState != null) {
            this.showingAll = savedInstanceState.getBoolean(SHOWING_ALL_BUNDLE_KEY, SHOWING_ALL_DEFAULT_VALUE);
        }

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
            dialogFragment.setSubmitListener(addItemListener);
            dialogFragment.show(getFragmentManager(), null);
            return true;
        }
        case (R.id.export_items_menu_item): {
            exportItems();
            return true;
        }
        case (R.id.import_items_menu_item): {
            chooseFileForImport();
            return true;
        }
        case (R.id.remove_all_items_menu_item): {
            ConfirmDialogFragment dialogFragment = ConfirmDialogFragment.newInstance(
                    R.string.remove_all_items__confirm_dialog__title, this.confirmListener);
            dialogFragment.show(getFragmentManager(), null);
            return true;
        }
        case (R.id.do_debug_action_menu_item): {
            // doDebugAction();
            return true;
        }
        }

        return false;
    }

    private void exportItems() {
        if (!IoUtils.isExternalStorageWritable()) {
            Toast.makeText(this, R.string.export_items__device_busy_error, Toast.LENGTH_SHORT).show();
            return;
        }
        File directory = getExternalFilesDir(null);
        File file = new File(directory, "shopper-items." + FILE_EXTENSION);
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

    private void importItems(String filePath) {
        List<String> lines = IoUtils.toStrings(new File(filePath));

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

    private void chooseFileForImport() {
        if (!IoUtils.isExternalStorageReadable()) {
            Toast.makeText(this, R.string.import_items__device_busy_error, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getBaseContext(), FileDialog.class);
        intent.putExtra(FileDialog.START_PATH, Environment.getExternalStorageDirectory().getPath());
        intent.putExtra(FileDialog.CAN_SELECT_DIR, false);
        intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);
        intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { FILE_EXTENSION });
        intent.putExtra(FileDialog.OPTION_ONE_CLICK_SELECT, true);
        startActivityForResult(intent, FILE_IMPORT_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((resultCode == Activity.RESULT_OK) && (requestCode == FILE_IMPORT_REQUEST_CODE)) {
            importItems(data.getStringExtra(FileDialog.RESULT_PATH));
        }
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

    private final class ConfirmDialogListener implements ConfirmDialogFragment.Listener {
        public void onConfirm() {
            removeAllItems();
            Toast.makeText(getApplicationContext(), R.string.remove_all_items__toast, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SHOWING_ALL_BUNDLE_KEY, showingAll);
    }

}
