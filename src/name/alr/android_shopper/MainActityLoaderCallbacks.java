package name.alr.android_shopper;

import name.alr.android_shopper.database.ShopItem;
import name.alr.android_shopper.database.ShopItemContentProvider;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.widget.SimpleCursorAdapter;

/**
 * @author alopezruiz@gmail.com (Alejandro Lopez Ruiz)
 */
public class MainActityLoaderCallbacks implements LoaderCallbacks<Cursor> {

    private static final String[] GET_LIST_ITEMS__COLUMNS = new String[] { ShopItem.ID + " as " + BaseColumns._ID,
            ShopItem.NAME, ShopItem.AMOUNT_TO_BUY };

    private static final String SHOW_ONLY_SELECTION_SQL = ShopItem.AMOUNT_TO_BUY + " > 0";

    private Context context;
    private SimpleCursorAdapter simpleCursorAdapter;

    public MainActityLoaderCallbacks(Context context, SimpleCursorAdapter simpleCursorAdapter) {
        this.context = context;
        this.simpleCursorAdapter = simpleCursorAdapter;
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = args.getBoolean(MainActivity.SHOWING_ALL_BUNDLE_KEY) ? null : SHOW_ONLY_SELECTION_SQL;
        return new CursorLoader(this.context, ShopItemContentProvider.ITEMS_CONTENT_URI, GET_LIST_ITEMS__COLUMNS,
                selection, null, ShopItem.SHOP_ORDER);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        this.simpleCursorAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        this.simpleCursorAdapter.swapCursor(null);
    }

}
