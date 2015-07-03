package is.hello.shiba.testing.suites;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import is.hello.buruberi.bluetooth.devices.SensePeripheral;
import is.hello.shiba.testing.IntegrationStep;
import is.hello.shiba.testing.IntegrationSuite;

import static is.hello.buruberi.bluetooth.devices.transmission.protobuf.SenseCommandProtos.wifi_endpoint.sec_type;

public class OnBoardingIntegrationSuite extends IntegrationSuite {
    private final SensePeripheral peripheral;
    private final Config config;

    public OnBoardingIntegrationSuite(@NonNull SensePeripheral peripheral, @NonNull Config config) {
        this.peripheral = peripheral;
        this.config = config;
    }

    private long createDelay(long delay, @NonNull TimeUnit unit) {
        if (config.flags.contains(Config.Flag.INCLUDE_DELAYS)) {
            if (config.flags.contains(Config.Flag.RANDOMIZE_DELAYS)) {
                return IntegrationStep.randomizeDelay(unit.toMillis(delay), 0.5f);
            } else {
                return unit.toMillis(delay);
            }
        } else {
            return 0;
        }
    }

    @Step(0) IntegrationStep connectToSense() {
        return IntegrationStep.withDelay("Connect",
                                         peripheral.connect(),
                                         createDelay(1, TimeUnit.MINUTES));
    }

    @Step(1) IntegrationStep getWiFiConnectivity() {
        return IntegrationStep.withoutDelay("Get WiFi Connectivity",
                peripheral.getWifiNetwork());
    }

    @Step(2) IntegrationStep scanForWiFiNetworks() {
        return IntegrationStep.withDelay("Scan for WiFi Networks",
                                         peripheral.connect(),
                                         createDelay(35, TimeUnit.SECONDS));
    }

    @Step(3) IntegrationStep sendWiFiCredentials() {
        return IntegrationStep.withDelay("Connect to WiFi Network",
                                         peripheral.setWifiNetwork(config.networkName, config.securityType, config.networkPassword),
                                         createDelay(10, TimeUnit.SECONDS));
    }

    @Step(4) IntegrationStep linkAccount() {
        return IntegrationStep.withoutDelay("Link Account",
                                            peripheral.linkAccount(config.accessToken));
    }

    @Step(5) IntegrationStep setTimeZone() {
        return null;
    }

    @Step(6) IntegrationStep pushData() {
        return IntegrationStep.withoutDelay("Link Account",
                                            peripheral.pushData());
    }

    @Step(7) IntegrationStep pairPill() {
        return IntegrationStep.withoutDelay("Pair Pill",
                                            peripheral.pairPill(config.accessToken));
    }

    @Step(8) IntegrationStep disconnectFromSense() {
        return IntegrationStep.withoutDelay("Disconnect",
                                            peripheral.disconnect());
    }


    public static class Config {
        public final String accessToken;
        public final sec_type securityType;
        public final String networkName;
        public final String networkPassword;
        public final EnumSet<Flag> flags;

        public Config(@NonNull String accessToken,
                      @NonNull sec_type securityType,
                      @NonNull String networkName,
                      @Nullable String networkPassword,
                      @NonNull EnumSet<Flag> flags) {
            this.accessToken = accessToken;
            this.securityType = securityType;
            this.networkName = networkName;
            this.networkPassword = networkPassword;
            this.flags = flags;
        }

        public enum Flag {
            INCLUDE_DELAYS,
            RANDOMIZE_DELAYS,
        }
    }
}
