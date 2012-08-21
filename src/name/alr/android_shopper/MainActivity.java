package name.alr.android_shopper;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * @author alopezruiz@gmail.com (Alejandro Lopez Ruiz)
 */
public class MainActivity extends Activity {

    private ShopperOpenHelper shopperOpenHelper;

    private SQLiteDatabase database;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate your view
        setContentView(R.layout.activity_main);

        this.shopperOpenHelper = new ShopperOpenHelper(this);
        this.database = this.shopperOpenHelper.getWritableDatabase();

        Cursor cursor = ShopperOpenHelper.getItems(this.database);

        startManagingCursor(cursor);

        // List<String> items = new ArrayList<String>(cursor.getCount());
        // if (cursor.moveToFirst()) {
        // do {
        // items.add(ShopperOpenHelper.getItemName(cursor));
        // cursor.moveToNext();
        // } while (!cursor.isAfterLast());
        // }
        // cursor.close();

        // Get references to UI widgets
        ListView listView = (ListView) findViewById(R.id.mainListView);

        // listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice,
        // items));

        // setListAdapter(new ArrayAdapter<String>(this,
        // android.R.layout.simple_list_item_multiple_choice, GENRES));

        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(this, R.layout.main_list_entry, cursor,
                new String[] { ShopperOpenHelper.NAME_COLUMN }, new int[] { R.id.mainListEntryText });
        listView.setAdapter(simpleCursorAdapter);

        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        this.database.close();

        super.onDestroy();
    }

}
