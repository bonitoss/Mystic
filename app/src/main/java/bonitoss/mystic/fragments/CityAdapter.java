package bonitoss.mystic.fragments;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

import bonitoss.mystic.WeatherManager;
import bonitoss.mystic.database.entities.City;

public class CityAdapter extends ArrayAdapter<City> {
    private final WeatherManager weatherManager;

    private final City[] cities;

    public CityAdapter(Context context, WeatherManager weatherManager) {
        super(context, android.R.layout.simple_list_item_1);

        this.cities = new City[0];

        this.weatherManager = weatherManager;
        this.weatherManager.getCities(this::addAll);
    }

    @Override
    public int getCount() {
        return this.cities.length;
    }

    @Override
    public City getItem(int position) {
        return this.cities[position];
    }

    @Override
    public int getPosition(City item) {
        return Arrays.binarySearch(this.cities, item);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view =  super.getView(position, convertView, parent);

        ((TextView)view.findViewById(android.R.id.text1)).setText(String.format("%s - %s", this.cities[position].Name, this.cities[position].Country));

        return view;
    }

    @Override
    public void addAll(City... cities) {
        super.addAll(cities);
    }
}
