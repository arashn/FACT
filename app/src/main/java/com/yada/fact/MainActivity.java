package com.yada.fact;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    replaceTopLevelFragment(new HomeFragment());
                    return true;
                case R.id.navigation_history:
                    replaceTopLevelFragment(new HistoryFragment());
                    return true;
                case R.id.navigation_recommendations:
                    replaceTopLevelFragment(new RecommendationsFragment());
                    return true;
                case R.id.navigation_settings:
                    replaceTopLevelFragment(new SettingsFragment());
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag("ROOT");
        if (fragment == null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragment = new HomeFragment();
            fragmentTransaction.add(R.id.fragment_container, fragment, "ROOT");
            fragmentTransaction.commit();
        }
    }

    private void replaceTopLevelFragment(Fragment newFragment) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragment_container, newFragment, "ROOT");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
    }

}
