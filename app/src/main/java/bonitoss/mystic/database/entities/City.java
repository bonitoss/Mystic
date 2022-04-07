package bonitoss.mystic.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class City {
    @PrimaryKey
    @NonNull
    public String Key;

    @ColumnInfo(name = "Name")
    public String Name;

    @ColumnInfo(name = "Country")
    public String Country;
}
