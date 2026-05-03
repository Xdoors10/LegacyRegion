package ru.dzhibrony.legacyregion.storage;

import com.mefrreex.jooq.JOOQConnector;
import com.mefrreex.jooq.database.IDatabase;
import com.mefrreex.jooq.database.MySQLDatabase;
import com.mefrreex.jooq.database.SQLiteDatabase;
import ru.dzhibrony.legacyregion.config.DatabaseSettings;

import java.io.File;

public final class DatabaseFactory {

    private DatabaseFactory() {
    }

    public static IDatabase create(DatabaseSettings settings, File dataFolder) {
        JOOQConnector.setJOOQMessagesEnabled(false);
        if (settings.mysql()) {
            return mysql(settings);
        }
        return sqlite(settings, dataFolder);
    }

    private static IDatabase mysql(DatabaseSettings settings) {
        return new MySQLDatabase(
                settings.mysqlHost(),
                settings.mysqlDatabase(),
                settings.mysqlUser(),
                settings.mysqlPassword()
        );
    }

    private static IDatabase sqlite(DatabaseSettings settings, File dataFolder) {
        return new SQLiteDatabase(new File(dataFolder, settings.sqliteFile()));
    }
}
