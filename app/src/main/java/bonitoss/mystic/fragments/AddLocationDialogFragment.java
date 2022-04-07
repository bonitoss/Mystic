package bonitoss.mystic.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import bonitoss.mystic.LocationHelper;
import bonitoss.mystic.R;
import bonitoss.mystic.WeatherManager;

public class AddLocationDialogFragment extends Fragment {

    private WeatherManager weatherManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocationHelper locationHelper = new LocationHelper(requireContext());

        locationHelper.getLocation(city -> {
            Toast.makeText(requireContext(), String.format(requireContext().getString(R.string.adding_city), city.Name), Toast.LENGTH_SHORT).show();
            weatherManager.addCity(city);

            requireActivity().startActivity(new Intent(requireContext(), requireActivity().getClass()));
            requireActivity().finish();
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        weatherManager = new WeatherManager(requireContext());
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_location, container, false);

        return view;
    }
}
