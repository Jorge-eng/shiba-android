package is.hello.shiba.logging;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import is.hello.buruberi.bluetooth.logging.LoggerFacade;

public class DatabaseLoggerFacade implements LoggerFacade {
    private static final DatabaseLoggerFacade INSTANCE = new DatabaseLoggerFacade();

    public static DatabaseLoggerFacade getInstance() {
        return INSTANCE;
    }


    //region Logging

    private void write(int level, @NonNull String tag, @NonNull String message) {
        LogDatabase.getInstance().write(level, tag, message);
        Log.println(level, tag, message);
    }

    private String formatMessage(@NonNull String message, @Nullable Throwable e) {
        if (e != null) {
            return message + "\n" + Log.getStackTraceString(e);
        } else {
            return message;
        }
    }

    @Override
    public void debug(@NonNull String tag, @NonNull String message) {
        write(Log.DEBUG, tag, message);
    }

    @Override
    public void info(@NonNull String tag, @NonNull String message) {
        write(Log.INFO, tag, message);
    }

    @Override
    public void warn(@NonNull String tag, @NonNull String message, @Nullable Throwable e) {
        write(Log.INFO, tag, formatMessage(message, e));
    }

    @Override
    public void error(@NonNull String tag, @NonNull String message, @Nullable Throwable e) {
        write(Log.INFO, tag, formatMessage(message, e));
    }

    //endregion
}
