package name.alr.android_shopper.database;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;

/**
 * @author alopezruiz@gmail.com (Alejandro Lopez Ruiz)
 */
public class ShopItemContentProvider extends ContentProvider {

    public static final String DECREASE_ITEM_AMOUNT_METHOD = "decreaseItemAmount";
    public static final String INCREASE_ITEM_AMOUNT_METHOD = "increaseItemAmount";
    public static final String LOWER_ITEM_METHOD = "lowerItem";
    public static final String RAISE_ITEM_METHOD = "raiseItem";

    /**
     * copypasted at AndroidManifest.xml
     */
    private static final String AUTHORITY = "name.alr.android_shopper.shopitem.contentprovider";
    public static final Uri ITEMS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + ShopItem.TABLE);

    private static final int ITEMS = 1;
    private static final int ITEM_ID = 2;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, ShopItem.TABLE, ITEMS);
        uriMatcher.addURI(AUTHORITY, ShopItem.TABLE + "/#", ITEM_ID);
    }

    private static Map<String, String> shopItemProjectionMap = new HashMap<String, String>();
    static {
        shopItemProjectionMap.put(ShopItem.ID, ShopItem.ID);
        shopItemProjectionMap.put(ShopItem.NAME, ShopItem.NAME);
        shopItemProjectionMap.put(ShopItem.AMOUNT_TO_BUY, ShopItem.AMOUNT_TO_BUY);
        shopItemProjectionMap.put(ShopItem.SHOP_ORDER, ShopItem.SHOP_ORDER);
    }

    private ShopperOpenHelper shopperOpenHelper;

    public static Uri getShopItemUri(long id) {
        return ContentUris.withAppendedId(ShopItemContentProvider.ITEMS_CONTENT_URI, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count;
        switch (uriMatcher.match(uri)) {
        case ITEMS:
            // count = db.delete(ShopItem.TABLE, selection, selectionArgs);
            count = this.shopperOpenHelper.deleteAllItems();
            break;
        case ITEM_ID:
            // count = db.delete(ShopItem.TABLE, BaseColumns._ID + " = " + uri.getLastPathSegment(), null);
            this.shopperOpenHelper.deleteItem(ContentUris.parseId(uri));
            count = 1;
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        notifyContentChange(uri);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
        case ITEMS:
            return " vnd.android.cursor.dir/name.alr.android_shopper.shopitem";
        case ITEM_ID:
            return " vnd.android.cursor.item/name.alr.android_shopper.shopitem";
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (uriMatcher.match(uri) != ITEMS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        long rowId = this.shopperOpenHelper.addItem(initialValues.getAsString(ShopItem.NAME),
                initialValues.getAsInteger(ShopItem.AMOUNT_TO_BUY));

        if (rowId > 0) {
            Uri itemUri = ContentUris.withAppendedId(ITEMS_CONTENT_URI, rowId);
            notifyContentChange(itemUri);
            return itemUri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate() {
        shopperOpenHelper = new ShopperOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(ShopItem.TABLE);
        qb.setProjectionMap(shopItemProjectionMap);

        Cursor cursor;
        switch (uriMatcher.match(uri)) {
        case ITEMS:
            SQLiteDatabase db = shopperOpenHelper.getReadableDatabase();
            cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            break;
        case ITEM_ID:
            cursor = this.shopperOpenHelper.getItem(ContentUris.parseId(uri));
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = shopperOpenHelper.getWritableDatabase();

        int count;
        switch (uriMatcher.match(uri)) {
        case ITEMS:
            count = db.update(ShopItem.TABLE, values, where, whereArgs);
            break;
        case ITEM_ID:
            db.update(ShopItem.TABLE, values, ShopItem.ID + " = ?", new String[] { uri.getLastPathSegment() });
            count = 1;
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        notifyContentChange(uri);
        return count;
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        if (INCREASE_ITEM_AMOUNT_METHOD.equals(method)) {
            increaseItemAmount(arg);
        } else if (DECREASE_ITEM_AMOUNT_METHOD.equals(method)) {
            decreaseItemAmount(arg);
        } else if (LOWER_ITEM_METHOD.equals(method)) {
            lowerItem(arg);
        } else if (RAISE_ITEM_METHOD.equals(method)) {
            raiseItem(arg);
        }
        return null;
    }

    private void increaseItemAmount(String arg) {
        long id = Long.parseLong(arg);
        this.shopperOpenHelper.increaseItemAmount(id);
        notifyContentChange(id);
    }

    private void decreaseItemAmount(String arg) {
        long id = Long.parseLong(arg);
        this.shopperOpenHelper.decreaseItemAmount(id);
        notifyContentChange(id);
    }

    private void raiseItem(String arg) {
        long id = Long.parseLong(arg);
        this.shopperOpenHelper.raiseItem(id);
        notifyContentChange();
    }

    private void lowerItem(String arg) {
        long id = Long.parseLong(arg);
        this.shopperOpenHelper.lowerItem(id);
        notifyContentChange();
    }

    private void notifyContentChange() {
        notifyContentChange(ITEMS_CONTENT_URI);
    }

    private void notifyContentChange(long id) {
        Uri uri = ContentUris.withAppendedId(ITEMS_CONTENT_URI, id);
        notifyContentChange(uri);
    }

    private void notifyContentChange(Uri uri) {
        getContext().getContentResolver().notifyChange(uri, null);
    }

}
