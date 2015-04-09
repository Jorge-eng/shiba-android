package is.hello.shiba.testing;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import is.hello.shiba.R;
import rx.Observable;
import rx.Observer;
import rx.Subscription;

public class IntegrationTestAdapter extends ArrayAdapter<IntegrationEvent> implements Observer<IntegrationEvent> {
    private final LayoutInflater inflater;

    private @Nullable Subscription currentRun;

    public IntegrationTestAdapter(@NonNull Context context) {
        super(context, R.layout.item_log);

        this.inflater = LayoutInflater.from(context);
    }


    //region Observer

    public void run(@NonNull Observable<IntegrationEvent> testRun) {
        if (currentRun != null) {
            currentRun.unsubscribe();
        }

        this.currentRun = testRun.subscribe(this);
    }

    @Override
    public void onCompleted() {}

    @Override
    public void onError(Throwable e) {
        add(new IntegrationEvent(IntegrationEvent.Type.RUN_FAILED, e));
    }

    @Override
    public void onNext(IntegrationEvent event) {
        add(event);
    }

    //endregion


    //region Views

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.item_log, parent, false);
        }

        TextView text = (TextView) view;
        IntegrationEvent event = getItem(position);
        text.setText(event.toMessage());

        return view;
    }

    //endregion
}
