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

    private static final String[] GET_ITEMS__COLUMNS = new String[] { ShopItem.ID + " as _id", ShopItem.NAME };

    public ShopperOpenHelper(Context context) {
        super(context, "main", null, 1);
    }

    public static String getItemName(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(ShopItem.NAME));
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ShopItem.TABLE + " (" + ShopItem.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ShopItem.NAME + " TEXT not null UNIQUE);");

        addTestData(db);
    }

    private static void addTestData(SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ShopItem.NAME, "Carrots");
        db.insertOrThrow(ShopItem.TABLE, null, contentValues);
        contentValues.put(ShopItem.NAME, "Tomatoes");
        db.insertOrThrow(ShopItem.TABLE, null, contentValues);
    }

    public static Cursor getItems(SQLiteDatabase db) {
        return db.query(ShopItem.TABLE, GET_ITEMS__COLUMNS, null, null, null, null, ShopItem.NAME + " COLLATE NOCASE");
        // return db.query(ShopItem.ITEM_TABLE, null, null, null, null, null, ShopItem.NAME_COLUMN);
    }

    public static void addItem(SQLiteDatabase db, String name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ShopItem.NAME, name);
        db.insertOrThrow(ShopItem.TABLE, null, contentValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // NOOP
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        if (!db.isReadOnly()) {
            // deleteAllItems(db);
            // addTestData(db);
        }
    }

    public static void deleteAllItems(SQLiteDatabase db) {
        db.delete(ShopItem.TABLE, null, null);
    }

}
