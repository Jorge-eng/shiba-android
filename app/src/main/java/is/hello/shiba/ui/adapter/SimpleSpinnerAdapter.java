package is.hello.shiba.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import is.hello.shiba.R;
import rx.functions.Func1;

public class SimpleSpinnerAdapter<T> extends ArrayAdapter<T> {
    private final LayoutInflater inflater;
    private final Func1<T, String> getTitle;

    public SimpleSpinnerAdapter(@NonNull Context context, @NonNull Func1<T, String> getTitle) {
        super(context, R.layout.item_simple_spinner);

        this.inflater = LayoutInflater.from(context);
        this.getTitle = getTitle;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.item_simple_spinner, parent, false);
        }

        TextView text = (TextView) view;
        T item = getItem(position);
        text.setText(getTitle.call(item));

        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.item_simple_spinner_dropdown, parent, false);
        }

        TextView text = (TextView) view;
        T item = getItem(position);
        text.setText(getTitle.call(item));

        return view;
    }
}
