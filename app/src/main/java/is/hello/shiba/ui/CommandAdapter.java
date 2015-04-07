package is.hello.shiba.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import is.hello.shiba.R;

public class CommandAdapter extends ArrayAdapter<CommandAdapter.Command> implements AbsListView.OnItemClickListener {
    private final LayoutInflater inflater;

    public CommandAdapter(@NonNull Context context) {
        super(context, R.layout.item_command);

        this.inflater = LayoutInflater.from(context);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Command command = (Command) parent.getItemAtPosition(position);
        if (command.enabled) {
            command.action.run();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.item_command, parent, false);
        }

        Command command = getItem(position);
        TextView text = (TextView) view;
        text.setText(command.titleRes);
        if (command.enabled) {
            text.setAlpha(0.8f);
        } else {
            text.setAlpha(0.8f);
        }

        return view;
    }


    public void addItem(@StringRes int titleRes, @NonNull Runnable action) {
        add(new Command(titleRes, action));
    }


    public class Command {
        public final @StringRes int titleRes;
        public final Runnable action;
        public boolean enabled = true;

        public Command(@StringRes int titleRes, @NonNull Runnable action) {
            this.titleRes = titleRes;
            this.action = action;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
            notifyDataSetChanged();
        }
    }
}
