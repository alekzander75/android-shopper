package name.alr.android_shopper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author alopezruiz@gmail.com (Alejandro Lopez Ruiz)
 */
public class ShopperOpenHelper extends SQLiteOpenHelper {

    private static final String ID_COLUMN = "id";
    private static final String NAME_COLUMN = "name";
    private static final String ITEM_TABLE = "item";

    public ShopperOpenHelper(Context context) {
        super(context, "main", null, 1);
    }

    public static String getItemName(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(NAME_COLUMN));
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ITEM_TABLE + " (" + ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NAME_COLUMN + " TEXT not null UNIQUE);");

        addTestData(db);
    }

    private static void addTestData(SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NAME_COLUMN, "Carrots");
        db.insertOrThrow(ITEM_TABLE, null, contentValues);
        contentValues.put(NAME_COLUMN, "Tomatoes");
        db.insertOrThrow(ITEM_TABLE, null, contentValues);
    }

    public static Cursor getItems(SQLiteDatabase db) {
//        return db.query(ITEM_TABLE, new String[] { ID_COLUMN, NAME_COLUMN }, null, null, null, null, NAME_COLUMN);
        return db.query(ITEM_TABLE, null, null, null, null, null, NAME_COLUMN);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // NOOP
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        if (!db.isReadOnly()) {
            db.delete(ITEM_TABLE, null, null);
            addTestData(db);
        }
    }

}
