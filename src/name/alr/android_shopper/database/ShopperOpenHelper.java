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

    private static final String SHOW_ONLY_SELECTION_SQL = ShopItem.AMOUNT_TO_BUY + " > 0";

    private static final int DUMMY_SHOP_ORDER = 0;

    private static final String REORDER_ITEMS_SQL = "update " + ShopItem.TABLE + " set " + ShopItem.SHOP_ORDER + " = ("
            + ShopItem.SHOP_ORDER + " - 1) where " + ShopItem.SHOP_ORDER + " > ?";

    private static final String INCREASE_ITEM_AMOUNT_SQL = "update " + ShopItem.TABLE + " set "
            + ShopItem.AMOUNT_TO_BUY + " = (" + ShopItem.AMOUNT_TO_BUY + " + 1) where " + ShopItem.ID + " = ?";

    private static final String DECREASE_ITEM_AMOUNT_SQL = "update " + ShopItem.TABLE + " set "
            + ShopItem.AMOUNT_TO_BUY + " = (" + ShopItem.AMOUNT_TO_BUY + " - 1) where " + ShopItem.ID + " = ?";

    private static final String DELETE_ITEM_SQL = ShopItem.ID + " = ?";

    private static final String MAX_ITEMS_SHOP_ORDER_SQL = "select max(" + ShopItem.SHOP_ORDER + ") from "
            + ShopItem.TABLE;

    private static final String[] GET_LIST_ITEMS__COLUMNS = new String[] { ShopItem.ID + " as _id", ShopItem.NAME,
            ShopItem.AMOUNT_TO_BUY };

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
                + ", " + ShopItem.AMOUNT_TO_BUY + " INTEGER not null" + ");");
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

    public static int getItemAmount(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(ShopItem.AMOUNT_TO_BUY));
    }

    public static String getItemName(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(ShopItem.NAME));
    }

    public Cursor getListItems(boolean showAll) {
        String selection = showAll ? null : SHOW_ONLY_SELECTION_SQL;
        return this.database.query(ShopItem.TABLE, GET_LIST_ITEMS__COLUMNS, selection, null, null, null,
                ShopItem.SHOP_ORDER);
        // return this.database.query(ShopItem.TABLE, GET_LIST_ITEMS__COLUMNS, null, null, null, null, ShopItem.NAME
        // + " COLLATE NOCASE");
    }

    public Cursor getItems() {
        return this.database.query(ShopItem.TABLE, null, null, null, null, null, ShopItem.SHOP_ORDER);
    }

    public void addItem(String name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ShopItem.NAME, name);
        contentValues.put(ShopItem.SHOP_ORDER, getMaxItemsShopOrder() + 1);
        contentValues.put(ShopItem.AMOUNT_TO_BUY, 1);
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

    public void increaseItemAmount(long id) {
        this.database.execSQL(INCREASE_ITEM_AMOUNT_SQL, new Object[] { id });
    }

    public void decreaseItemAmount(long id) {
        this.database.execSQL(DECREASE_ITEM_AMOUNT_SQL, new Object[] { id });
    }

    public void raiseItem(long id) {
        int itemShopOrder = getItemShopOrder(getItem(id));
        int otherItemShopOrder = itemShopOrder - 1;

        swapShopPositions(id, itemShopOrder, otherItemShopOrder);
    }

    private void swapShopPositions(long id, int itemShopOrder, int otherItemShopOrder) {
        this.database.beginTransaction();
        try {
            ContentValues contentValues = new ContentValues();

            contentValues.put(ShopItem.SHOP_ORDER, DUMMY_SHOP_ORDER);
            this.database.update(ShopItem.TABLE, contentValues, ShopItem.SHOP_ORDER + " = ?",
                    new String[] { Integer.toString(otherItemShopOrder) });

            contentValues.put(ShopItem.SHOP_ORDER, otherItemShopOrder);
            this.database.update(ShopItem.TABLE, contentValues, ShopItem.ID + " = ?",
                    new String[] { Long.toString(id) });

            contentValues.put(ShopItem.SHOP_ORDER, itemShopOrder);
            this.database.update(ShopItem.TABLE, contentValues, ShopItem.SHOP_ORDER + " = ?",
                    new String[] { Integer.toString(DUMMY_SHOP_ORDER) });

            this.database.setTransactionSuccessful();
        } finally {
            this.database.endTransaction();
        }
    }

    public void lowerItem(long id) {
        int itemShopOrder = getItemShopOrder(getItem(id));
        int otherItemShopOrder = itemShopOrder + 1;

        swapShopPositions(id, itemShopOrder, otherItemShopOrder);
    }

}
