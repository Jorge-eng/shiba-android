package is.hello.shiba.testing;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import java.util.Arrays;

public final class IntegrationEvent {
    private final Type type;
    private final Object[] parameters;

    public IntegrationEvent(@NonNull Type type, @NonNull Object... parameters) {
        this.type = type;
        this.parameters = parameters;
    }


    //region Attributes

    public @NonNull Type getType() {
        return type;
    }

    public @NonNull Object[] getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "IntegrationEvent{" +
                "type=" + type +
                ", parameters=" + Arrays.toString(parameters) +
                '}';
    }

    //endregion


    //region Pretty Printing

    private CharSequence formatAsError(@NonNull CharSequence rawMessage) {
        SpannableString message = new SpannableString(rawMessage);
        message.setSpan(new ForegroundColorSpan(Color.RED), 0, message.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return message;
    }

    public CharSequence toMessage() {
        String type = getType().name;
        String parameters = TextUtils.join(" ", getParameters());
        String message = type + " " + parameters;
        if (getType().isError) {
            return formatAsError(message);
        } else {
            return message;
        }
    }

    //endregion


    public static enum Type {
        RUN_WILL_START(false, "Run starting"),
        RUN_FAILED(true, "Run failed"),
        RUN_COMPLETED(false, "Run completed"),

        STEP_WILL_RUN(false, "Step starting"),
        STEP_COMPLETED(false, "Step completed"),
        STEP_FAILED(true, "Step failed");

        public final boolean isError;
        public final String name;

        private Type(boolean isError, @NonNull String name) {
            this.isError = isError;
            this.name = name;
        }
    }
}
