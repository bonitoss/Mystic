package bonitoss.mystic.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import bonitoss.mystic.database.entities.Forecast;

@Dao
public interface ForecastDao {

    @Query("SELECT * FROM forecast")
    List<Forecast> getAll();

    @Insert
    void insert(Forecast forecast);

    @Delete
    void delete(Forecast forecast);

    @Query("SELECT * FROM forecast WHERE Key LIKE :key")
    List<Forecast> findByCity(String key);

    @Query("DELETE FROM forecast WHERE Key LIKE :key")
    void removeByCity(String key);

    @Insert
    void addAll(Forecast[] forecasts);
}
