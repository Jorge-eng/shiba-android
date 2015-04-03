package is.hello.shiba.graph;

import android.app.Application;

import dagger.ObjectGraph;
import is.hello.buruberi.bluetooth.BuruberiModule;
import is.hello.buruberi.bluetooth.logging.LogCatLoggerFacade;
import is.hello.shiba.api.ApiModule;

public class ShibaApplication extends Application {
    private static ShibaApplication instance = null;

    private ObjectGraph graph;

    public static ShibaApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ShibaApplication.instance = this;
        buildGraph();
    }

    private void buildGraph() {
        this.graph = ObjectGraph.create(
            new ApiModule(),
            new BuruberiModule(this, e -> {}, new LogCatLoggerFacade()),
            new AppModule(this)
        );
    }

    public <T> T inject(T instance) {
        return graph.inject(instance);
    }
}
