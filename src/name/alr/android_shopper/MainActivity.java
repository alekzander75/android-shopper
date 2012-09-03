package name.alr.android_shopper;

import name.alr.android_shopper.database.ShopItem;
import name.alr.android_shopper.database.ShopperOpenHelper;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * @author alopezruiz@gmail.com (Alejandro Lopez Ruiz)
 */
public class MainActivity extends Activity {

    private ShopperOpenHelper shopperOpenHelper;

    private EditText itemEditText;
    private boolean addingNew;
    private ListView listView;
    private BaseAdapter listAdapter;
    private Cursor itemsCursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

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

        this.itemEditText = (EditText) findViewById(R.id.itemEditText);

        this.itemEditText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        MainActivity.this.shopperOpenHelper.addItem(MainActivity.this.itemEditText.getText().toString()
                                .trim());
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
        getMenuInflater().inflate(R.menu.main_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
        case (R.id.add_item_menu_item): {
            addNewItem();
            return true;
        }
        case (R.id.remove_item_menu_item): {
            if (this.addingNew) {
                cancelAdd();
            } else {
                removeItem(this.listView.getSelectedItemId());
            }
            return true;
        }
        case (R.id.remove_all_items_menu_item): {
            if (this.addingNew) {
                cancelAdd();
            } else {
                removeAllItems();
            }
            return true;
        }
        }

        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        int idx = this.listView.getSelectedItemPosition();

        String removeTitle = getString(this.addingNew ? R.string.cancel : R.string.remove);

        MenuItem removeItem = menu.findItem(R.id.remove_item_menu_item);
        removeItem.setTitle(removeTitle);
        removeItem.setVisible(this.addingNew || idx > -1);

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

    private void removeAllItems() {
        this.shopperOpenHelper.deleteAllItems();
        MainActivity.this.itemsCursor.requery();
    }

    private void addNewItem() {
        this.addingNew = true;
        this.itemEditText.setVisibility(View.VISIBLE);
        this.itemEditText.requestFocus();
    }

    private void removeItem(long id) {
        this.shopperOpenHelper.deleteItem(id);
        MainActivity.this.itemsCursor.requery();
    }

    @Override
    protected void onDestroy() {
        this.shopperOpenHelper.closeDatabase();

        super.onDestroy();
    }

    private void cancelAdd() {
        this.addingNew = false;
        this.itemEditText.setVisibility(View.GONE);
    }

}
