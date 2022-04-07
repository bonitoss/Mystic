package bonitoss.mystic.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import bonitoss.mystic.database.entities.City;

@Dao
public interface CityDao {

    @Query("SELECT * FROM city")
    List<City> getAll();

    @Query("SELECT * FROM city WHERE Name LIKE :name AND Country LIKE :country LIMIT 1")
    City findByName(String name, String country);

    @Insert
    void insert(City city);

    @Delete
    void delete(City city);

    @Query("SELECT * FROM city WHERE Key LIKE :key LIMIT 1")
    City findByKey(String key);
}
