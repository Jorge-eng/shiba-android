package is.hello.shiba.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import is.hello.shiba.R;
import is.hello.shiba.graph.ShibaFragment;
import is.hello.shiba.ui.fragments.TestRunFragment;

public class MainActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentById(R.id.activity_home_container) == null) {
            fm.beginTransaction()
              .add(R.id.activity_home_container, new TestRunFragment())
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

    @Override
    public void onBackPressed() {
        ShibaFragment topFragment = getTopFragment();
        if (topFragment == null || !topFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }


    public @Nullable ShibaFragment getTopFragment() {
        return (ShibaFragment) getSupportFragmentManager().findFragmentById(R.id.activity_home_container);
    }

    public void pushFragment(@NonNull ShibaFragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_home_container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(fragment.getClass().getName())
                .commit();
    }
}
