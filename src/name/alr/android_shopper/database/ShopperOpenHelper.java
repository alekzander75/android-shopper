package name.alr.android_shopper.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author alopezruiz@gmail.com (Alejandro Lopez Ruiz)
 */
public class ShopperOpenHelper extends SQLiteOpenHelper {

    private static final String REORDER_ITEMS_SQL = "update " + ShopItem.TABLE + " set " + ShopItem.SHOP_ORDER + " = ("
            + ShopItem.SHOP_ORDER + " - 1) where " + ShopItem.SHOP_ORDER + " > ?";

    private static final String DELETE_ITEM_SQL = ShopItem.ID + " = ?";

    private static final String MAX_ITEMS_SHOP_ORDER_SQL = "select max(" + ShopItem.SHOP_ORDER + ") from "
            + ShopItem.TABLE;

    private static final String[] GET_ITEMS__COLUMNS = new String[] { ShopItem.ID + " as _id", ShopItem.NAME };

    private SQLiteDatabase database;

    public ShopperOpenHelper(Context context) {
        super(context, "main.db", null, 1);
    }

    public void initialize() {
        this.database = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ShopItem.TABLE + " (" + ShopItem.ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + ", "
                + ShopItem.NAME + " TEXT not null UNIQUE" + ", " + ShopItem.SHOP_ORDER + " INTEGER not null UNIQUE"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // NOOP
    }

    public void closeDatabase() {
        this.database.close();
    }

    public static int getItemShopOrder(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(ShopItem.SHOP_ORDER));
    }

    public Cursor getItems() {
        return this.database.query(ShopItem.TABLE, GET_ITEMS__COLUMNS, null, null, null, null, ShopItem.SHOP_ORDER);
        // return this.database.query(ShopItem.TABLE, GET_ITEMS__COLUMNS, null, null, null, null, ShopItem.NAME
        // + " COLLATE NOCASE");
        // return db.query(ShopItem.ITEM_TABLE, null, null, null, null, null, ShopItem.NAME_COLUMN);
    }

    public void addItem(String name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ShopItem.NAME, name);
        contentValues.put(ShopItem.SHOP_ORDER, getMaxItemsShopOrder() + 1);
        this.database.insertOrThrow(ShopItem.TABLE, null, contentValues);
    }

    private int getMaxItemsShopOrder() {
        Cursor cursor = this.database.rawQuery(MAX_ITEMS_SHOP_ORDER_SQL, null);
        int result = 0;
        if (cursor.moveToFirst()) {
            result = cursor.getInt(0);
        }
        cursor.close();
        return result;
    }

    /**
     * @return positioned {@link Cursor}, or <code>null</code>. remeber to {@link Cursor#close()} it.
     */
    private Cursor getItem(long id) {
        Cursor cursor = this.database.query(ShopItem.TABLE, null, ShopItem.ID + " = ?",
                new String[] { Long.toString(id) }, null, null, null);
        if (cursor.moveToFirst()) {
            return cursor;
        } else {
            cursor.close();
            return null;
        }
    }

    public void deleteAllItems() {
        this.database.delete(ShopItem.TABLE, null, null);
    }

    public void deleteItem(long id) {
        Cursor itemCursor = getItem(id);
        int itemShopOrder = getItemShopOrder(itemCursor);
        itemCursor.close();

        this.database.beginTransaction();
        try {
            this.database.delete(ShopItem.TABLE, DELETE_ITEM_SQL, new String[] { Long.toString(id) });
            this.database.execSQL(REORDER_ITEMS_SQL, new String[] { Integer.toString(itemShopOrder) });
            this.database.setTransactionSuccessful();
        } finally {
            this.database.endTransaction();
        }
    }

}
