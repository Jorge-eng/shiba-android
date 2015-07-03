package is.hello.shiba.graph;

import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import is.hello.buruberi.bluetooth.Buruberi;
import is.hello.buruberi.bluetooth.stacks.BluetoothStack;
import is.hello.buruberi.bluetooth.stacks.util.LoggerFacade;

@Module(complete = false, library = true)
public class BluetoothModule {
    @Singleton @Provides BluetoothStack provideBluetoothStack(@NonNull Context context,
                                                              @NonNull LoggerFacade loggerFacade) {
        return new Buruberi()
                .setApplicationContext(context)
                .setLoggerFacade(loggerFacade)
                .build();
    }
}
