package is.hello.shiba.logging;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import is.hello.buruberi.bluetooth.stacks.util.LoggerFacade;

public class DatabaseLoggerFacade implements LoggerFacade {
    private static final DatabaseLoggerFacade INSTANCE = new DatabaseLoggerFacade();

    public static DatabaseLoggerFacade getInstance() {
        return INSTANCE;
    }


    //region Logging

    private void write(int level, @NonNull String tag, @Nullable String message) {
        LogDatabase.getInstance().write(level, tag, message);
        Log.println(level, tag, message);
    }

    private String formatMessage(@Nullable String message, @Nullable Throwable e) {
        if (e != null) {
            return message + "\n" + Log.getStackTraceString(e);
        } else {
            return message;
        }
    }

    @Override
    public void debug(@NonNull String tag, @Nullable String message) {
        write(Log.DEBUG, tag, message);
    }

    @Override
    public void info(@NonNull String tag, @Nullable String message) {
        write(Log.INFO, tag, message);
    }

    @Override
    public void warn(@NonNull String tag, @Nullable String message, @Nullable Throwable e) {
        write(Log.WARN, tag, formatMessage(message, e));
    }

    @Override
    public void warn(@NonNull String tag, @Nullable String message) {
        write(Log.WARN, tag, message);
    }

    @Override
    public void error(@NonNull String tag, @Nullable String message, @Nullable Throwable e) {
        write(Log.ERROR, tag, formatMessage(message, e));
    }

    //endregion
}
