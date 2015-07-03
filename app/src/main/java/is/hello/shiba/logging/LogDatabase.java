package is.hello.shiba.logging;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.joda.time.DateTimeUtils;

public class LogDatabase extends SQLiteOpenHelper {
    //region Constants

    public static final String TABLE_LOGS = "logs";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LEVEL = "level";
    public static final String COLUMN_TAG = "tag";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_TIME = "time";
    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_LEVEL, COLUMN_TAG, COLUMN_MESSAGE, COLUMN_TIME};

    private static final String CREATE = ("CREATE TABLE " + TABLE_LOGS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_LEVEL + " INTEGER, " +
            COLUMN_TAG + " TEXT NOT NULL, " +
            COLUMN_MESSAGE + " TEXT NOT NULL, " +
            COLUMN_TIME + " INTEGER" +
            ");");
    private static final int VERSION = 1;

    //endregion


    //region Singleton

    private static LogDatabase instance;

    public static void init(@NonNull Context context) {
        LogDatabase.instance = new LogDatabase(context.getApplicationContext());
    }

    public static LogDatabase getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Must call init before getInstance");
        }

        return instance;
    }

    //endregion


    //region Lifecycle

    private final Handler workQueue;

    public LogDatabase(@NonNull Context context) {
        super(context, TABLE_LOGS, null, VERSION);

        HandlerThread handlerThread = new HandlerThread(getClass().getSimpleName());
        handlerThread.start();
        this.workQueue = new Handler(handlerThread.getLooper());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGS);
        onCreate(db);
    }

    //endregion


    //region Data

    public Cursor getLogs(@Nullable Integer limit) {
        String queryLimit = limit != null ? limit.toString() : null;
        return getWritableDatabase().query(TABLE_LOGS, ALL_COLUMNS, null, null, null, null, COLUMN_ID + " DESC", queryLimit);
    }

    public void clear() {
        workQueue.post(() -> {
            try {
                getWritableDatabase().delete(TABLE_LOGS, null, null);
            } catch (SQLiteException e) {
                Log.e(getClass().getSimpleName(), "Could not erase logs.", e);
            }
        });
    }

    public void write(int level, @NonNull String tag, @Nullable String message) {
        workQueue.post(() -> {
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put(COLUMN_LEVEL, level);
                contentValues.put(COLUMN_TAG, tag);
                contentValues.put(COLUMN_MESSAGE, message == null ? "" : message);
                contentValues.put(COLUMN_TIME, DateTimeUtils.currentTimeMillis());
                getWritableDatabase().insert(TABLE_LOGS, null, contentValues);
            } catch (SQLiteException e) {
                Log.e(getClass().getSimpleName(), "Could not write to log.", e);
            }
        });
    }

    //endregion
}
