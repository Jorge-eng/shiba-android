package is.hello.shiba.graph;

import android.app.Application;

import dagger.ObjectGraph;
import is.hello.buruberi.bluetooth.BuruberiModule;
import is.hello.shiba.api.ApiModule;
import is.hello.shiba.logging.DatabaseLoggerFacade;
import is.hello.shiba.logging.LogDatabase;

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
        LogDatabase.init(this);
        this.graph = ObjectGraph.create(
            new ApiModule(),
            new BuruberiModule(this, e -> {}, DatabaseLoggerFacade.getInstance()),
            new AppModule(this)
        );
    }

    public <T> T inject(T instance) {
        return graph.inject(instance);
    }
}
