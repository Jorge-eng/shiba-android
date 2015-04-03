package is.hello.shiba.api.model;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.joda.time.DateTime;
import org.joda.time.Hours;

import java.util.HashMap;
import java.util.Map;

public class Device {
    public static final int MISSING_THRESHOLD_HRS = 24;

    @JsonProperty("type")
    private Type type;

    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty("state")
    private State state;

    @JsonProperty("firmware_version")
    private String firmwareVersion;

    @JsonProperty("last_updated")
    private DateTime lastUpdated;

    @JsonProperty("color")
    private Color color;

    @JsonIgnore
    private boolean exists = true;


    //region Util

    public static Map<Type, Device> getDevicesMap(@NonNull Iterable<Device> devices) {
        Map<Type, Device> map = new HashMap<>();
        for (Device device : devices) {
            map.put(device.getType(), device);
        }
        return map;
    }

    //endregion


    //region Creation

    public static Device createPlaceholder(@NonNull Type type) {
        Device device = new Device();
        device.type = type;
        device.exists = false;
        device.state = State.UNKNOWN;
        return device;
    }

    //endregion


    public Type getType() {
        return type;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public State getState() {
        return state;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public DateTime getLastUpdated() {
        return lastUpdated;
    }

    public Color getColor() {
        return color;
    }

    @JsonIgnore
    public boolean exists() {
        return exists;
    }

    /**
     * Returns the number of hours since the device was last updated.
     * <p/>
     * Returns 0 if the device has not reported being update yet. This state
     * happens immediately after a device has been paired to an account.
     */
    @JsonIgnore
    public int getHoursSinceLastUpdated() {
        if (lastUpdated != null) {
            return Hours.hoursBetween(lastUpdated, DateTime.now()).getHours();
        } else {
            return 0;
        }
    }

    /**
     * Returns whether or not the device is considered to be missing.
     * <p/>
     * Differs from {@link #getHoursSinceLastUpdated()} by considering
     * a missing last updated value to indicate a device is missing.
     */
    @JsonIgnore
    public boolean isMissing() {
        return (!exists || (getLastUpdated() == null) ||
                (getHoursSinceLastUpdated() >= MISSING_THRESHOLD_HRS));
    }

    @Override
    public String toString() {
        return "Device{" +
                "type=" + type +
                ", deviceId='" + deviceId + '\'' +
                ", state=" + state +
                ", firmwareVersion='" + firmwareVersion + '\'' +
                ", lastUpdated=" + lastUpdated +
                ", color=" + color +
                ", exists=" + exists +
                '}';
    }


    public static enum Type {
        PILL,
        SENSE,
        OTHER;


        @JsonCreator
        public static Type fromString(@NonNull String string) {
            return Enums.fromString(string, values(), OTHER);
        }
    }

    public static enum State {
        NORMAL,
        LOW_BATTERY,
        UNKNOWN;

        @JsonCreator
        public static State fromString(@NonNull String string) {
            return Enums.fromString(string, values(), UNKNOWN);
        }
    }

    public static enum Color {
        BLACK,
        WHITE,
        BLUE,
        RED,
        UNKNOWN;

        @JsonCreator
        public static Color fromString(@NonNull String string) {
            return Enums.fromString(string, values(), UNKNOWN);
        }
    }
}
