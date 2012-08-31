package name.alr.android_shopper;

import name.alr.android_shopper.database.ShopItem;
import name.alr.android_shopper.database.ShopperOpenHelper;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * @author alopezruiz@gmail.com (Alejandro Lopez Ruiz)
 */
public class MainActivity extends Activity {

    private static final int ADD_NEW_ITEM = Menu.FIRST;
    private static final int REMOVE_ITEM = Menu.FIRST + 1;
    private static final int REMOVE_ALL_ITEM = Menu.FIRST + 2;

    private ShopperOpenHelper shopperOpenHelper;

    private SQLiteDatabase database;
    private EditText itemEditText;
    private boolean addingNew;
    private ListView listView;
    private BaseAdapter listAdapter;
    private Cursor itemsCursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate your view
        setContentView(R.layout.activity_main);

        this.shopperOpenHelper = new ShopperOpenHelper(this);
        this.database = this.shopperOpenHelper.getWritableDatabase();

        this.itemsCursor = ShopperOpenHelper.getItems(this.database);

        startManagingCursor(this.itemsCursor);

        // List<String> items = new ArrayList<String>(cursor.getCount());
        // if (cursor.moveToFirst()) {
        // do {
        // items.add(ShopperOpenHelper.getItemName(cursor));
        // cursor.moveToNext();
        // } while (!cursor.isAfterLast());
        // }
        // cursor.close();

        this.listView = (ListView) findViewById(R.id.mainListView);

        // listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice,
        // items));

        // setListAdapter(new ArrayAdapter<String>(this,
        // android.R.layout.simple_list_item_multiple_choice, GENRES));

        this.listAdapter = new SimpleCursorAdapter(this, R.layout.main_list_entry, this.itemsCursor,
                new String[] { ShopItem.NAME }, new int[] { R.id.mainListEntryText });
        this.listView.setAdapter(this.listAdapter);

        this.listView.setItemsCanFocus(false);
        this.listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        registerForContextMenu(this.listView);

        this.itemEditText = (EditText) findViewById(R.id.itemEditText);

        this.itemEditText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        ShopperOpenHelper.addItem(MainActivity.this.database, MainActivity.this.itemEditText.getText()
                                .toString().trim());
                        MainActivity.this.itemEditText.setText("");
                        MainActivity.this.itemsCursor.requery();
                        cancelAdd();
                        return true;
                    }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.activity_main, menu);

        super.onCreateOptionsMenu(menu);

        // Create and add new menu items.
        MenuItem itemAdd = menu.add(0, ADD_NEW_ITEM, Menu.NONE, R.string.add_new);
        MenuItem itemRem = menu.add(0, REMOVE_ITEM, Menu.NONE, R.string.remove);
        MenuItem removeAllItem = menu.add(0, REMOVE_ALL_ITEM, Menu.NONE, R.string.remove_all);

        // Assign icons
        itemAdd.setIcon(R.drawable.add_new_item);
        itemRem.setIcon(R.drawable.remove_item);
        removeAllItem.setIcon(R.drawable.remove_item);

        // Allocate shortcuts to each of them.
        itemAdd.setShortcut('0', 'a');
        itemRem.setShortcut('1', 'r');
        removeAllItem.setShortcut('2', 'x');

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        int idx = this.listView.getSelectedItemPosition();

        String removeTitle = getString(this.addingNew ? R.string.cancel : R.string.remove);

        MenuItem removeItem = menu.findItem(REMOVE_ITEM);
        removeItem.setTitle(removeTitle);
        removeItem.setVisible(this.addingNew || idx > -1);

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.setHeaderTitle("Selected Item");
        menu.add(0, REMOVE_ITEM, Menu.NONE, R.string.remove);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        int index = this.listView.getSelectedItemPosition();

        switch (item.getItemId()) {
        case (REMOVE_ITEM): {
            if (this.addingNew) {
                cancelAdd();
            } else {
                removeItem(index);
            }
            return true;
        }
        case (REMOVE_ALL_ITEM): {
            if (this.addingNew) {
                cancelAdd();
            } else {
                removeAllItems();
            }
            return true;
        }
        case (ADD_NEW_ITEM): {
            addNewItem();
            return true;
        }
        }

        return false;
    }

    private void removeAllItems() {
        ShopperOpenHelper.deleteAllItems(this.database);
        MainActivity.this.itemsCursor.requery();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);

        switch (item.getItemId()) {
        case (REMOVE_ITEM): {
            AdapterView.AdapterContextMenuInfo menuInfo;
            menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            int index = menuInfo.position;

            removeItem(index);
            return true;
        }
        }
        return false;
    }

    private void addNewItem() {
        this.addingNew = true;
        this.itemEditText.setVisibility(View.VISIBLE);
        this.itemEditText.requestFocus();
    }

    private void removeItem(int index) {
        // TODO: delete rom from table here
        // todoItems.remove(_index);
        MainActivity.this.itemsCursor.requery();
    }

    @Override
    protected void onDestroy() {
        this.database.close();

        super.onDestroy();
    }

    private void cancelAdd() {
        this.addingNew = false;
        this.itemEditText.setVisibility(View.GONE);
    }

}
