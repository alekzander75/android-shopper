package name.alr.android_shopper.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author alopezruiz@gmail.com (Alejandro Lopez Ruiz)
 */
class ShopperOpenHelper extends SQLiteOpenHelper {

    private static final String ITEMS_TO_REORDER_SELECTION_SQL = ShopItem.SHOP_ORDER + " > ?";

    private static final int DUMMY_SHOP_ORDER = -1;

    private static final String INCREASE_ITEM_AMOUNT_SQL = "update " + ShopItem.TABLE + " set "
            + ShopItem.AMOUNT_TO_BUY + " = (" + ShopItem.AMOUNT_TO_BUY + " + 1) where " + ShopItem.ID + " = ?";

    private static final String DECREASE_ITEM_AMOUNT_SQL = "update " + ShopItem.TABLE + " set "
            + ShopItem.AMOUNT_TO_BUY + " = (" + ShopItem.AMOUNT_TO_BUY + " - 1) where " + ShopItem.ID + " = ?";

    private static final String DELETE_ITEM_SQL = ShopItem.ID + " = ?";

    private static final String MAX_ITEMS_SHOP_ORDER_SQL = "select max(" + ShopItem.SHOP_ORDER + ") from "
            + ShopItem.TABLE;

    private static final String[] GET_ITEMS_TO_REORDER__COLUMNS = new String[] { ShopItem.ID, ShopItem.SHOP_ORDER };

    public ShopperOpenHelper(Context context) {
        super(context, "main.db", null, 1);
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
        getWritableDatabase().close();
    }

    private static int getItemShopOrder(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(ShopItem.SHOP_ORDER));
    }

    private Cursor getItemsToReorder(int deletedItemShopOrder) {
        return getReadableDatabase().query(ShopItem.TABLE, GET_ITEMS_TO_REORDER__COLUMNS,
                ITEMS_TO_REORDER_SELECTION_SQL, new String[] { Integer.toString(deletedItemShopOrder) }, null, null,
                ShopItem.SHOP_ORDER);
    }

    public long addItem(String name, int amount) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ShopItem.NAME, name);
        contentValues.put(ShopItem.SHOP_ORDER, getMaxItemsShopOrder() + 1);
        contentValues.put(ShopItem.AMOUNT_TO_BUY, amount);
        return getWritableDatabase().insertOrThrow(ShopItem.TABLE, null, contentValues);
    }

    private int getMaxItemsShopOrder() {
        Cursor cursor = getReadableDatabase().rawQuery(MAX_ITEMS_SHOP_ORDER_SQL, null);
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
    public Cursor getItem(long id) {
        Cursor cursor = getReadableDatabase().query(ShopItem.TABLE, null, ShopItem.ID + " = ?",
                new String[] { Long.toString(id) }, null, null, null);
        if (cursor.moveToFirst()) {
            return cursor;
        } else {
            cursor.close();
            return null;
        }
    }

    public int deleteAllItems() {
        return getWritableDatabase().delete(ShopItem.TABLE, null, null);
    }

    public void deleteItem(long id) {
        SQLiteDatabase database = getWritableDatabase();

        Cursor itemCursor = getItem(id);
        int itemShopOrder = getItemShopOrder(itemCursor);
        itemCursor.close();

        database.beginTransaction();
        try {
            database.delete(ShopItem.TABLE, DELETE_ITEM_SQL, new String[] { Long.toString(id) });

            Cursor cursor = getItemsToReorder(itemShopOrder);
            if (cursor.moveToFirst()) {
                do {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(ShopItem.SHOP_ORDER, getItemShopOrder(cursor) - 1);
                    database.update(ShopItem.TABLE, contentValues, ShopItem.ID + " = ?",
                            new String[] { Long.toString(cursor.getLong(0)) });

                    cursor.moveToNext();
                } while (!cursor.isAfterLast());
            }
            cursor.close();

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public void increaseItemAmount(long id) {
        getWritableDatabase().execSQL(INCREASE_ITEM_AMOUNT_SQL, new Object[] { id });
    }

    public void decreaseItemAmount(long id) {
        getWritableDatabase().execSQL(DECREASE_ITEM_AMOUNT_SQL, new Object[] { id });
    }

    public void raiseItem(long id) {
        int itemShopOrder = getItemShopOrder(getItem(id));
        int otherItemShopOrder = itemShopOrder - 1;

        swapShopPositions(id, itemShopOrder, otherItemShopOrder);
    }

    private void swapShopPositions(long id, int itemShopOrder, int otherItemShopOrder) {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues contentValues = new ContentValues();

            contentValues.put(ShopItem.SHOP_ORDER, DUMMY_SHOP_ORDER);
            database.update(ShopItem.TABLE, contentValues, ShopItem.SHOP_ORDER + " = ?",
                    new String[] { Integer.toString(otherItemShopOrder) });

            contentValues.put(ShopItem.SHOP_ORDER, otherItemShopOrder);
            database.update(ShopItem.TABLE, contentValues, ShopItem.ID + " = ?", new String[] { Long.toString(id) });

            contentValues.put(ShopItem.SHOP_ORDER, itemShopOrder);
            database.update(ShopItem.TABLE, contentValues, ShopItem.SHOP_ORDER + " = ?",
                    new String[] { Integer.toString(DUMMY_SHOP_ORDER) });

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public void lowerItem(long id) {
        int itemShopOrder = getItemShopOrder(getItem(id));
        int otherItemShopOrder = itemShopOrder + 1;

        swapShopPositions(id, itemShopOrder, otherItemShopOrder);
    }

}
