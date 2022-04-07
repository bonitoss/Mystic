package bonitoss.mystic.fragments;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.function.Consumer;

import bonitoss.mystic.R;
import bonitoss.mystic.activities.MainActivity;
import bonitoss.mystic.database.entities.City;

public class LocationSelectorDialogFragment extends DialogFragment {

    private final List<City> cities;
    private final Consumer<City> consumer;

    public LocationSelectorDialogFragment(List<City> cities, Consumer<City> consumer) {
        this.cities = cities;
        this.consumer = consumer;
    }

    public static final String TAG = "LocationSelectorDialogFragment";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String[] items = new String[cities.size()];

        cities.forEach(city -> items[cities.indexOf(city)] = city.Name);

        if(items.length == 0) {
            return new MaterialAlertDialogBuilder(getContext())
                    .setTitle(R.string.location_selector_dialog_title)
                    .setMessage(R.string.location_selector_dialog_empty)
                    .setCancelable(false)
                    .setPositiveButton(R.string.location_selector_dialog_add_new, (dialog, which) -> {
                        ((MainActivity) getActivity()).showAddLocationDialog();
                    })
                    .create();
        }

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.location_selector_dialog_title)
                .setItems(items, (dialog, which) -> {
                    City city = cities.get(which);
                    this.consumer.accept(city);
                })
                .setCancelable(false)
                .setPositiveButton(R.string.location_selector_dialog_add_new, (dialog, which) -> {
                    ((MainActivity) requireActivity()).showAddLocationDialog();
                })
                .create();
    }

}
