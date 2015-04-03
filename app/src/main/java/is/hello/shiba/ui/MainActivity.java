package is.hello.shiba.ui;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import is.hello.shiba.R;
import is.hello.shiba.graph.ShibaFragment;

public class MainActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentById(R.id.activity_home_container) == null) {
            fm.beginTransaction()
              .add(R.id.activity_home_container, new LandingFragment())
              .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    public void pushFragment(@NonNull ShibaFragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_home_container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(fragment.getClass().getName())
                .commit();
    }
}
