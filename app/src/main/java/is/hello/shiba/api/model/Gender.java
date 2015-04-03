package is.hello.shiba.api.model;

import android.support.annotation.Nullable;

public enum Gender {
    MALE,
    FEMALE,
    OTHER;

    public static Gender fromString(@Nullable String string) {
        return Enums.fromString(string, values(), OTHER);
    }
}
