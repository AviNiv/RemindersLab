package il.co.aviniv.reminderslab;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.security.cert.CollectionCertStoreParameters;

/**
 * Created by Avi on 19/08/2015.
 */
public class RemindersDbAdapter {

    //these are the column names
    public static final String COL_ID = "_id";
    public static final String COL_CONTENT = "_content";
    public static final String COL_IMPORTANT = "_important";

    //these are the corresponding indices
    public static final int INDEX_ID = 0;
    public static final int INDEX_CONTENT = INDEX_ID + 1;
    public static final int INDEX_IMPORTANT = INDEX_ID + 2;

    //used for logging
    private static final String TAG = "RemindersDbAdapter";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "dba_remdrs";
    private static final String TABLE_NAME = "tbl_remdrs";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    //SQL statement used to create the database
    private static final String DATABASE_CREATE = "CREATE TABLE if not exists " + TABLE_NAME
            + " ( " + COL_ID + " INTEGER PRIMARY KEY autoincrement"
            + ", " + COL_CONTENT + " TEXT"
            + ", " + COL_IMPORTANT + " INTEGER"
            + " );";



    public RemindersDbAdapter(Context ctx) {
        mCtx = ctx;
    }

    public void open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
    }

    public void closer() throws SQLException {
        if(mDbHelper != null) {
            mDb.close();
        }
    }



    //CREATE
    //note that the id will be created for you automatically
    public void createReminder(String name, boolean important) {
        ContentValues values = new ContentValues();
        values.put(COL_CONTENT, name);
        values.put(COL_IMPORTANT, important);
        mDb.insert(TABLE_NAME, null, values);
    }

    //overloaded to take a reminder
    public long createReminder(Reminder reminder) {
        ContentValues values = new ContentValues();
        values.put(COL_CONTENT,  reminder.getContent());
        values.put(COL_IMPORTANT, reminder.getImportant());

        // Inserting Row
        return mDb.insert(TABLE_NAME,  null, values);
    }

    //READ
    public Reminder fetchReminderById(int id) {
        Cursor cursor = mDb.query(TABLE_NAME
                , new String[]{COL_ID, COL_CONTENT,  COL_IMPORTANT}, COL_ID + "=?"
                , new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        return new Reminder(cursor.getInt(INDEX_ID), cursor.getString(INDEX_CONTENT), cursor.getInt(INDEX_IMPORTANT));
    }

    public Cursor fetchAllReminders() {
        Cursor mCursor = mDb.query(TABLE_NAME, new String[]{COL_ID, COL_CONTENT,  COL_IMPORTANT}, null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        return mCursor;
    }

    //UPDATE
    public void updateReminder(Reminder reminder) {
        ContentValues values = new ContentValues();
        values.put(COL_CONTENT, reminder.getContent());
        values.put(COL_IMPORTANT, reminder.getImportant());
        mDb.update(TABLE_NAME, values, COL_ID + "=?", new String[]{String.valueOf(reminder.getId())});
    }

    //DELETE
    public void deleteReminderById(int nId) {
        mDb.delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(nId)});
    }

    public void deleteAllReminders() {
        mDb.delete(TABLE_NAME, null, null);
    }


    //--------//--------//--------//--------//--------//

    private static class DatabaseHelper extends SQLiteOpenHelper {

        Context mCxt;

        DatabaseHelper(Context context) {
            super (context, DATABASE_NAME, null, DATABASE_VERSION);
            mCxt = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                Log.w(TAG, DATABASE_CREATE);
                db.execSQL(DATABASE_CREATE);
            }
            catch (Exception ex) {
                Utilities.showException(mCxt, ex);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String upgradeMessage = "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data";
            Log.w(TAG, upgradeMessage);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
