package bonitoss.mystic.database;

import android.content.Context;

import androidx.room.Room;


public class DatabaseManager {

    private final Context context;

    private AppDatabase appDatabase;

    public DatabaseManager(Context context) {
        this.context = context;
    }

    public AppDatabase getDatabase() {
        if(appDatabase == null) {
            appDatabase = Room.databaseBuilder(context,
                    AppDatabase.class, "database").build();
        }

        if(!appDatabase.isOpen()) {
            appDatabase = Room.databaseBuilder(context,
                    AppDatabase.class, "database").build();
        }

        return appDatabase;
    }
}
