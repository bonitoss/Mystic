package bonitoss.mystic;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import bonitoss.mystic.database.DatabaseManager;
import bonitoss.mystic.database.entities.City;
import bonitoss.mystic.database.entities.Forecast;

public class WeatherManager {

    private final static String API_HOST = "https://api.weatherapi.com/v1";
    private final static SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

    private final Context context;

    private DatabaseManager databaseManager;
    private RequestQueue requestQueue;

    public WeatherManager(Context context) {
        this.context = context;

        this.databaseManager = new DatabaseManager(context);
        this.requestQueue = Volley.newRequestQueue(context);
    }

    /**
     * Get all the cities from the database.
     * @param consumer The consumer to call when the cities are ready.
     */
    public void getCities(Consumer<List<City>> consumer) {
        new Thread(() -> {
            Looper.prepare();

            consumer.accept(databaseManager.getDatabase().cityDao().getAll());
        }).start();
    }

    /**
     * Initializes a new city from the name and country.
     * @param name The name of the city.
     * @param country The country of the city.
     * @return The city that was created.
     */
    public City constructCity(String name, String country) {
        City city = new City();
        city.Key = UUID.nameUUIDFromBytes((name + "-" + country).getBytes()).toString();
        city.Name = name;
        city.Country = country;

        return city;
    }

    /**
     * Adds a city to the database.
     * @param city The city to add.
     */
    public void addCity(City city) {
        new Thread(() -> {
            Looper.prepare();

            databaseManager.getDatabase().cityDao().insert(city);
        }).start();
    }

    /**
     * Gets the forecast for the city.
     * @param city The city to get the forecast for.
     * @param consumer The consumer to call when the forecast is ready.
     */
    public void getForecast(City city, Consumer<Forecast> consumer) {
        new Thread(() -> {
            Looper.prepare();

            try {
                List<Forecast> forecasts = databaseManager.getDatabase().forecastDao().findByCity(city.Key);

                if (forecasts.size() == 0) {
                    if(!networkAvailable()) {
                        consumer.accept(null);
                        return;
                    }

                    Log.i("WeatherManager", "Forecast not found in database, fetching from API");
                    getForecastFromAPI(city, consumer);
                    return;
                }

                // Find the newest forecast, and make sure it isn't older than 3 hours.
                Forecast newestForecast = null;
                for (Forecast forecast : forecasts) {
                    if (newestForecast == null || FORMATTER.parse(newestForecast.Date).before(FORMATTER.parse(forecast.Date))) {
                        newestForecast = forecast;
                    }
                }

                Date date = FORMATTER.parse(newestForecast.Date);

                if (date.getTime() + 3 * 60 * 60 * 1000 < new Date().getTime()) {
                    if(!networkAvailable()) {
                        consumer.accept(newestForecast);
                        return;
                    }

                    getForecastFromAPI(city, consumer);
                    return;
                }

                consumer.accept(newestForecast);
            }catch (ParseException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Clears the local forecast for the city.
     * @param city The city to clear the forecast for.
     */
    public void clearForecast(City city) {
        new Thread(() -> {
            Looper.prepare();

            List<Forecast> forecasts = databaseManager.getDatabase().forecastDao().findByCity(city.Key);
            for(Forecast f : forecasts) {
                databaseManager.getDatabase().forecastDao().delete(f);
            }
        }).start();
    }

    /**
     * Gets a city from the database by its key.
     * @param cityKey The key of the city.
     */
    public void getCityByKey(String cityKey, Consumer<City> consumer) {
        new Thread(() -> {
            Looper.prepare();

            consumer.accept(databaseManager.getDatabase().cityDao().findByKey(cityKey));
        }).start();
    }

    private void getForecastFromAPI(City city, Consumer<Forecast> consumer) {
        new Thread(() -> {
            Looper.prepare();

            try {
                URL url = new URL(String.format("%s/current.json?q=%s&key=%s", API_HOST, city.Name, context.getString(R.string.api_key)));

                JsonObjectRequest request = new JsonObjectRequest(url.toString(), response -> {
                    try {
                        Forecast forecast = new Forecast();
                        forecast.Id = UUID.randomUUID().toString();
                        forecast.Key = city.Key;
                        forecast.Date = FORMATTER.format(new Date());
                        forecast.Temperature = response.getJSONObject("current").getDouble("temp_c");
                        forecast.FeelsLike = response.getJSONObject("current").getDouble("feelslike_c");
                        forecast.Unit = "C";
                        forecast.Description = response.getJSONObject("current").getJSONObject("condition").getString("text");

                        new Thread(() -> {
                            Looper.prepare();

                            databaseManager.getDatabase().forecastDao().insert(forecast);
                        }).start();

                        consumer.accept(forecast);
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, Throwable::printStackTrace);

                requestQueue.add(request);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void getFuture(City city, Consumer<List<Forecast>> consumer, int days) {
        new Thread(() -> {
            Looper.prepare();

            try {
                // Get the forecasts from the database.
                List<Forecast> forecasts = databaseManager.getDatabase().forecastDao().findByCity(city.Key);

                // If there are no forecasts, get them from the API.
                if (forecasts.size() < days) {
                    if(!networkAvailable()) {
                        consumer.accept(forecasts);
                        return;
                    }

                    Log.i("ForecastManager", "No forecasts found, getting from API.");
                    getFutureForecastsFromAPI(city, consumer, days);
                    return;
                }

                // If there are forecasts, but they are not up to date, get them from the API.
                Date date = FORMATTER.parse(forecasts.get(0).Date);
                if (date.getTime() + 3 * 60 * 60 * 1000 < new Date().getTime()) {
                    if(!networkAvailable()) {
                        consumer.accept(forecasts);
                        return;
                    }

                    Log.i("ForecastManager", "Forecasts are out of date, getting from API.");
                    getFutureForecastsFromAPI(city, consumer, days);
                    return;
                }

                // If there are forecasts and they are up to date, return them.
                consumer.accept(forecasts);
            }catch (ParseException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void getFutureForecastsFromAPI(City city, Consumer<List<Forecast>> consumer, int days) {
        new Thread(() -> {
            Looper.prepare();

            try {
                URL url = new URL(String.format("%s/forecast.json?key=%s&q=%s&days=%s&aqi=false&alerts=yes", API_HOST, context.getString(R.string.api_key), city.Name, days));

                JsonObjectRequest request = new JsonObjectRequest(url.toString(), response -> {
                    try {
                        List<Forecast> forecasts = new ArrayList<>();

                        JSONArray forecastArray = response.getJSONObject("forecast").getJSONArray("forecastday");

                        for(int i = 0; i < forecastArray.length(); i++) {
                            JSONObject forecast = forecastArray.getJSONObject(i);

                            Forecast f = new Forecast();
                            f.Id = UUID.randomUUID().toString();
                            f.Key = city.Key;
                            f.Date = forecast.getString("date");
                            f.Temperature = forecast.getJSONObject("day").getDouble("avgtemp_c");
                            f.Unit = "C";
                            f.Description = forecast.getJSONObject("day").getJSONObject("condition").getString("text");

                            forecasts.add(f);
                        }

                        consumer.accept(forecasts);

                        new Thread(() -> {
                            databaseManager.getDatabase().forecastDao().findByCity(city.Key).forEach(forecast -> {
                                new Thread(() -> {
                                    Looper.prepare();

                                    databaseManager.getDatabase().forecastDao().delete(forecast);
                                }).start();
                            });

                            databaseManager.getDatabase().forecastDao().addAll(forecasts.toArray(new Forecast[0]));
                        }).start();
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, Throwable::printStackTrace);

                requestQueue.add(request);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private boolean networkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static Date parseDate(String date) {
        try {
            return FORMATTER.parse(date);
        }catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }
}
