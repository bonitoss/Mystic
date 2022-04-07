package bonitoss.mystic.fragments;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormatSymbols;
import java.util.Date;
import java.util.List;

import bonitoss.mystic.R;
import bonitoss.mystic.WeatherManager;
import bonitoss.mystic.database.entities.Forecast;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {
    private final List<Forecast> forecasts;

    public ForecastAdapter(List<Forecast> forecasts) {
        this.forecasts = forecasts;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private View view;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
        }

        @SuppressLint("StringFormatMatches")
        public void update(Forecast forecast) {
            TextView forecastTitle = view.findViewById(R.id.forecast_title);
            TextView forecastSubtitle = view.findViewById(R.id.forecast_subtitle);
            TextView forecastDescription = view.findViewById(R.id.forecast_description);

            Date date = WeatherManager.parseDate(forecast.Date);

            String dateString = new DateFormatSymbols().getMonths()[date.getMonth()-1] + " " + date.getDay() + "th";

            forecastTitle.setText(String.format("%s %s", dateString, String.format("%s°%s", forecast.Temperature, forecast.Unit)));
            forecastSubtitle.setText(String.format(view.getContext().getString(R.string.feels_like), forecast.FeelsLike, forecast.Unit));
            forecastDescription.setText(forecast.Description);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.forecast_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Forecast forecast = this.forecasts.get(position);

        holder.update(forecast);
    }

    @Override
    public int getItemCount() {
        return this.forecasts.size();
    }
}
