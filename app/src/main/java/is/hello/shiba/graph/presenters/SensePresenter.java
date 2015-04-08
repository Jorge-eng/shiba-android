package is.hello.shiba.graph.presenters;

import android.support.annotation.NonNull;
import android.util.Pair;

import javax.inject.Inject;
import javax.inject.Singleton;

import is.hello.buruberi.bluetooth.devices.SensePeripheral;
import rx.Observable;
import rx.subjects.ReplaySubject;

@Singleton public class SensePresenter {
    private final ApiPresenter api;

    public final ReplaySubject<SensePeripheral> peripheral = ReplaySubject.createWithSize(1);

    @Inject SensePresenter(@NonNull ApiPresenter api) {
        this.api = api;
    }


    public Observable<SensePeripheral.ConnectStatus> connectTo(@NonNull SensePeripheral newPeripheral) {
        return newPeripheral.connect().doOnCompleted(() -> this.peripheral.onNext(newPeripheral));
    }

    public Observable<Void> disconnect() {
        return peripheral.<SensePeripheral>flatMap(SensePeripheral::disconnect).map(ignored -> null);
    }

    public Observable<String> pairPill() {
        Observable<Pair<SensePeripheral, String>> union = Observable.combineLatest(peripheral, api.accessToken, Pair::new);
        return union.flatMap(p -> p.first.pairPill(p.second));
    }

    public Observable<Void> linkAccount() {
        Observable<Pair<SensePeripheral, String>> union = Observable.combineLatest(peripheral, api.accessToken, Pair::new);
        return union.flatMap(p -> p.first.linkAccount(p.second));
    }
}
