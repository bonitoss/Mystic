package bonitoss.mystic.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import bonitoss.mystic.database.daos.CityDao;
import bonitoss.mystic.database.daos.ForecastDao;
import bonitoss.mystic.database.entities.City;
import bonitoss.mystic.database.entities.Forecast;

@Database(entities = {City.class, Forecast.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CityDao cityDao();
    public abstract ForecastDao forecastDao();
}