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

    private SQLiteDatabase database;

    public ShopperOpenHelper(Context context) {
        super(context, "main", null, 1);
    }

    public void initialize() {
        this.database = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ShopItem.TABLE + " (" + ShopItem.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ShopItem.NAME + " TEXT not null UNIQUE);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // NOOP
    }

    public void closeDatabase() {
        this.database.close();
    }

    // public static String getItemName(Cursor cursor) {
    // return cursor.getString(cursor.getColumnIndexOrThrow(ShopItem.NAME));
    // }

    public Cursor getItems() {
        return this.database.query(ShopItem.TABLE, GET_ITEMS__COLUMNS, null, null, null, null, ShopItem.NAME
                + " COLLATE NOCASE");
        // return db.query(ShopItem.ITEM_TABLE, null, null, null, null, null, ShopItem.NAME_COLUMN);
    }

    public void addItem(String name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ShopItem.NAME, name);
        this.database.insertOrThrow(ShopItem.TABLE, null, contentValues);
    }

    public void deleteAllItems() {
        this.database.delete(ShopItem.TABLE, null, null);
    }

    public void deleteItem(long id) {
        this.database.delete(ShopItem.TABLE, ShopItem.ID + " = ?", new String[] { Long.toString(id) });
    }

}
