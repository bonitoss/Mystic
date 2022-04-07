package bonitoss.mystic.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.room.Room;

import android.os.Bundle;
import android.view.View;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.DynamicColors;

import bonitoss.mystic.R;
import bonitoss.mystic.WeatherManager;
import bonitoss.mystic.database.AppDatabase;
import bonitoss.mystic.fragments.AddLocationDialogFragment;
import bonitoss.mystic.fragments.ForecastFragment;
import bonitoss.mystic.fragments.LoadingFragment;
import bonitoss.mystic.fragments.LocationSelectorDialogFragment;
import bonitoss.mystic.fragments.MainFragment;

public class MainActivity extends AppCompatActivity {

    private WeatherManager weatherManager;
    private AppDatabase db;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply Dynamic colors (Android 12+)
        DynamicColors.applyToActivitiesIfAvailable(getApplication());
        DynamicColors.applyIfAvailable(this);

        setContentView(R.layout.activity_main);

        // Set up the splash screen
        setUpSplashScreen();

        // Set up the database
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database").build();
        // Set up the request queue
        requestQueue = Volley.newRequestQueue(this);

        // Set up the weather manager
        weatherManager = new WeatherManager(this);


        FragmentManager fragmentManager = getSupportFragmentManager();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_main) {
                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, new MainFragment())
                        .commit();
                return true;
            }else if(item.getItemId() == R.id.menu_forecast) {
                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, new ForecastFragment())
                        .commit();
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            fragmentManager.beginTransaction().replace(R.id.fragmentContainer, new LoadingFragment()).commit();

            boolean shouldShowLocationSelector = getSharedPreferences("prefs", MODE_PRIVATE).getBoolean("shouldShowLocationSelector", true);

            if (shouldShowLocationSelector) {
                bottomNavigationView.setVisibility(View.GONE);
                // Show the location selector
                weatherManager.getCities(cities -> {
                    runOnUiThread(() -> {
                        new LocationSelectorDialogFragment(cities, city -> {
                            getSharedPreferences("prefs", MODE_PRIVATE).edit().putString("city", city.Key).apply();

                            bottomNavigationView.setVisibility(View.VISIBLE);
                            fragmentManager.beginTransaction()
                                    .replace(R.id.fragmentContainer, new MainFragment())
                                    .commit();
                        }).show(fragmentManager, LocationSelectorDialogFragment.TAG);
                    });
                });
                return;
            }

            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, new MainFragment())
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        if (bottomNavigationView.getVisibility() == View.VISIBLE && bottomNavigationView.getSelectedItemId() != R.id.menu_main) {
            bottomNavigationView.setSelectedItemId(R.id.menu_main);
            return;
        }

        super.onBackPressed();
    }

    private void setUpSplashScreen() {

    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    public void showCitySelector() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragmentContainer, new LoadingFragment()).commit();
    }

    public AppDatabase getDb() {
        return db;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public void showAddLocationDialog() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new LoadingFragment()).commit();

        runOnUiThread(() -> {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new AddLocationDialogFragment()).commit();
        });
    }
}