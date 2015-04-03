package is.hello.shiba.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import is.hello.shiba.R;
import rx.functions.Func1;

public class SimpleAdapter<T> extends ArrayAdapter<T> {
    private final Func1<T, String> getTitle;
    private final Func1<T, String> getDetails;
    private final LayoutInflater inflater;

    public SimpleAdapter(@NonNull Context context,
                         @NonNull Func1<T, String> getTitle,
                         @Nullable Func1<T, String> getDetails) {
        super(context, R.layout.item_detail);

        this.getTitle = getTitle;
        this.getDetails = getDetails;
        this.inflater = LayoutInflater.from(context);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.item_detail, parent, false);
            view.setTag(new ViewHolder(view));
        }

        //noinspection unchecked
        ViewHolder holder = (ViewHolder) view.getTag();
        T item = getItem(position);
        holder.title.setText(getTitle.call(item));
        if (getDetails != null) {
            holder.details.setText(getDetails.call(item));
        } else {
            holder.details.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    class ViewHolder {
        final TextView title;
        final TextView details;

        ViewHolder(@NonNull View view) {
            this.title = (TextView) view.findViewById(R.id.item_detail_title);
            this.details = (TextView) view.findViewById(R.id.item_detail_details);
        }
    }
}
