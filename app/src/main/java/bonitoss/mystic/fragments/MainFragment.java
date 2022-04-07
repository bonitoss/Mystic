package bonitoss.mystic.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.function.Consumer;

import bonitoss.mystic.WeatherManager;
import bonitoss.mystic.activities.MainActivity;
import bonitoss.mystic.R;
import bonitoss.mystic.database.entities.City;
import bonitoss.mystic.database.entities.Forecast;

public class MainFragment extends Fragment {

    private WeatherManager weatherManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void clearWeather(@NonNull City city) {
        weatherManager.clearForecast(city);
    }

    private void getWeather(@NonNull City city) {
        ((MainActivity) requireActivity()).setActionBarTitle(String.format(requireContext().getString(R.string.weather_in), city.Name));

        // Let's get the weather
        weatherManager.getForecast(city, new Consumer<Forecast>() {
            @Override
            public void accept(Forecast forecast) {
                // Update the UI
                updateUI(forecast);
            }
        });
    }

    private void updateUI(@NonNull Forecast forecast) {
        if(getView() == null) return;

        requireActivity().runOnUiThread(() -> {
            SwipeRefreshLayout swipeRefreshLayout = getView().findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setRefreshing(false);

            TextView temperature = getView().findViewById(R.id.temperature);
            TextView description = getView().findViewById(R.id.description);

            temperature.setText(String.format("%s°%s", forecast.Temperature, forecast.Unit));
            description.setText(String.format("%s, %s", forecast.Description, String.format(requireContext().getString(R.string.feels_like), forecast.FeelsLike, forecast.Unit)));
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        weatherManager = new WeatherManager(requireContext());
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        ((SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout)).setRefreshing(true);
        
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        String cityKey = sharedPreferences.getString("city", null);
        if(cityKey != null) {
            weatherManager.getCityByKey(cityKey, (city) -> {
                requireActivity().runOnUiThread(() -> {
                    SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
                    swipeRefreshLayout.setOnRefreshListener(() -> {
                        clearWeather(city);
                        getWeather(city);
                    });

                    if(city != null) {
                        getWeather(city);
                    }else {
                        // City is null, we need to ask the user to select a city
                        ((MainActivity) getActivity()).showCitySelector();
                    }
                });
            });
        }

        return view;
    }

}