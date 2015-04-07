package is.hello.shiba.ui.util;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import is.hello.buruberi.bluetooth.devices.HelloPeripheral;
import is.hello.buruberi.bluetooth.devices.transmission.protobuf.SenseCommandProtos;
import is.hello.shiba.R;

public class Styles {
    public static @StringRes int getConnectStatusStringRes(@NonNull HelloPeripheral.ConnectStatus status) {
        switch (status) {
            case CONNECTING:
                return R.string.status_connecting;

            case DISCOVERING_SERVICES:
                return R.string.status_discovering_services;

            default:
                return 0;
        }
    }

    public static @StringRes int getSecTypeStringRes(@NonNull SenseCommandProtos.wifi_endpoint.sec_type securityType) {
        switch (securityType) {
            case SL_SCAN_SEC_TYPE_OPEN:
                return R.string.security_type_open;

            case SL_SCAN_SEC_TYPE_WEP:
                return R.string.security_type_wep;

            case SL_SCAN_SEC_TYPE_WPA:
                return R.string.security_type_wpa;

            case SL_SCAN_SEC_TYPE_WPA2:
                return R.string.security_type_wpa2;

            default:
                return 0;
        }
    }
}
