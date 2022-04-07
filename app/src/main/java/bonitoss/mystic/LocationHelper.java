package bonitoss.mystic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

import java.util.function.Consumer;

import bonitoss.mystic.activities.MainActivity;
import bonitoss.mystic.database.entities.City;
import bonitoss.mystic.R;

public class LocationHelper {

    private final Context context;
    private final WeatherManager weatherManager;
    private final RequestQueue requestQueue;

    public LocationHelper(Context context) {
        this.context = context;
        this.weatherManager = new WeatherManager(context);
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public void getLocation(Consumer<City> consumer) {
        if(hasPermission()) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, getLocationListener(locationManager, consumer));
        }else {
            // Request permission
            if(context instanceof MainActivity) {
                Toast.makeText(context, R.string.location_permission_required, Toast.LENGTH_LONG).show();

                ActivityCompat.requestPermissions((MainActivity) context, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
                getLocation(consumer);
                return;
            }

            Toast.makeText(context, R.string.no_location_permission_cant_grant, Toast.LENGTH_SHORT).show();
            consumer.accept(null);
        }
    }

    private LocationListener getLocationListener(LocationManager locationManager, Consumer<City> consumer) {
        return new LocationListener() {

            @Override
            public void onLocationChanged(@NonNull Location location) {
                locationManager.removeUpdates(this);

                String url = "https://nominatim.openstreetmap.org/reverse?lat=" + location.getLatitude() + "&lon=" + location.getLongitude() + "&format=json";

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url,  response -> {
                    try {
                        String city = response.getJSONObject("address").getString("city");
                        String country = response.getJSONObject("address").getString("country");

                        consumer.accept(weatherManager.constructCity(city, country));
                    }catch (JSONException e) {
                        e.printStackTrace();
                        consumer.accept(null);
                    }
                }, Throwable::printStackTrace);

                requestQueue.add(jsonObjectRequest);
            }
        };
    }

    private boolean hasPermission() {
        return context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

}
