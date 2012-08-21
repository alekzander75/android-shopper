package name.alr.android_shopper;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * @author alopezruiz@gmail.com (Alejandro Lopez Ruiz)
 */
public class MainActivity extends ListActivity {

    private ShopperOpenHelper shopperOpenHelper;

    private SQLiteDatabase database;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setContentView(R.layout.activity_main);

        this.shopperOpenHelper = new ShopperOpenHelper(this);
        this.database = this.shopperOpenHelper.getWritableDatabase();

        Cursor cursor = ShopperOpenHelper.getItems(this.database);
        List<String> items = new ArrayList<String>(cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
                items.add(ShopperOpenHelper.getItemName(cursor));
                cursor.moveToNext();
            } while (!cursor.isAfterLast());
        }
        cursor.close();

        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, items));

        // setListAdapter(new ArrayAdapter<String>(this,
        // android.R.layout.simple_list_item_multiple_choice, GENRES));

        // setListAdapter(new CursorAdapter(this, cursor, 0) {
        //
        // @Override
        // public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
        // // TODO Auto-generated method stub
        // return null;
        // }
        //
        // @Override
        // public void bindView(View arg0, Context arg1, Cursor arg2) {
        // // TODO Auto-generated method stub
        //
        // }
        // });

        final ListView listView = getListView();

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
