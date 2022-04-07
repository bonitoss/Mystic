package bonitoss.mystic.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Forecast {
    @PrimaryKey
    @NonNull
    public String Id;

    @NonNull
    public String Key;

    @ColumnInfo(name = "Date")
    public String Date;

    @ColumnInfo(name = "Temperature")
    public double Temperature;

    @ColumnInfo(name = "FeelsLike")
    public double FeelsLike;

    @ColumnInfo(name = "Unit")
    public String Unit;

    @ColumnInfo(name = "Description")
    public String Description;
}
