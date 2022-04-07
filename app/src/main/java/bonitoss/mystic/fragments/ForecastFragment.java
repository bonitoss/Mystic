package bonitoss.mystic.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

import bonitoss.mystic.R;
import bonitoss.mystic.WeatherManager;
import bonitoss.mystic.activities.MainActivity;
import bonitoss.mystic.database.entities.City;
import bonitoss.mystic.database.entities.Forecast;

public class ForecastFragment extends Fragment {

    private WeatherManager weatherManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    private void getForecasts(@NonNull City city) {
        ((MainActivity) requireActivity()).setActionBarTitle(String.format(requireContext().getString(R.string.forecast_for), city.Name));

        weatherManager.getFuture(city, (forecasts) -> {
            requireActivity().runOnUiThread(() -> {
                updateUI(forecasts);
            });
        }, 3);
    }

    private void updateUI(@NonNull List<Forecast> forecasts) {
        if (getView() == null) return;

        SwipeRefreshLayout swipeRefreshLayout = getView().findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setRefreshing(false);

        // Update the RecyclerView
        RecyclerView recyclerView = getView().findViewById(R.id.recyclerView);

        recyclerView.setAdapter(new ForecastAdapter(forecasts));
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        weatherManager = new WeatherManager(requireContext());
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_forecast, container, false);

        // Get all forecasts
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        String cityKey = sharedPreferences.getString("city", null);
        if (cityKey != null) {
            weatherManager.getCityByKey(cityKey, (city) -> {
                requireActivity().runOnUiThread(() -> {
                    SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
                    swipeRefreshLayout.setOnRefreshListener(() -> {
                        getForecasts(city);
                    });

                    if (city != null) {
                        getForecasts(city);
                    } else {
                        // City is null, we need to ask the user to select a city
                        ((MainActivity) getActivity()).showCitySelector();
                    }
                });
            });
        }

        return view;
    }
}
