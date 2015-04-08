package is.hello.shiba.logging;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import is.hello.shiba.R;

public class LogAdapter extends CursorAdapter {
    private static final int FETCH_LIMIT = 200;

    private final LayoutInflater inflater;
    private final Resources resources;

    public LogAdapter(@NonNull Context context) {
        super(context, LogDatabase.getInstance().getLogs(FETCH_LIMIT), FLAG_REGISTER_CONTENT_OBSERVER);

        this.inflater = LayoutInflater.from(context);
        this.resources = context.getResources();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.item_log, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView text = (TextView) view;
        int level = cursor.getInt(cursor.getColumnIndex(LogDatabase.COLUMN_LEVEL));
        text.setText(getFormattedMessage(cursor));
        text.setTextColor(logLevelToColor(level));
    }

    public DateTime getDate(int position) {
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        long time = cursor.getLong(cursor.getColumnIndex(LogDatabase.COLUMN_TIME));
        return new DateTime(time, DateTimeZone.getDefault());
    }

    public String getFormattedMessage(Cursor cursor) {
        String message = cursor.getString(cursor.getColumnIndex(LogDatabase.COLUMN_MESSAGE));
        String tag = cursor.getString(cursor.getColumnIndex(LogDatabase.COLUMN_TAG));
        int level = cursor.getInt(cursor.getColumnIndex(LogDatabase.COLUMN_LEVEL));
        String levelString = logLevelToString(level);
        return (levelString + "/" + tag + ": " + message);
    }

    public String getFormattedMessage(int position) {
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        return getFormattedMessage(cursor);
    }


    private static String logLevelToString(int level) {
        switch (level) {
            case Log.VERBOSE:
                return "V";

            case Log.DEBUG:
                return "D";

            case Log.INFO:
                return "I";

            case Log.WARN:
                return "W";

            case Log.ERROR:
                return "E";

            case Log.ASSERT:
                return "A";

            default:
                return "";
        }
    }

    private int logLevelToColor(int level) {
        switch (level) {
            case Log.DEBUG:
                return resources.getColor(R.color.log_debug);

            case Log.INFO:
                return resources.getColor(R.color.log_info);

            case Log.WARN:
                return resources.getColor(R.color.log_warning);

            case Log.ASSERT:
            case Log.ERROR:
                return resources.getColor(R.color.log_error);

            default:
                return Color.BLACK;
        }
    }
}
