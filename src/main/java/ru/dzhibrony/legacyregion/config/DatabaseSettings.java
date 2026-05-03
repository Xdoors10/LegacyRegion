package ru.dzhibrony.legacyregion.config;

public record DatabaseSettings(
        String type,
        String sqliteFile,
        String mysqlHost,
        String mysqlDatabase,
        String mysqlUser,
        String mysqlPassword
) {

    public boolean mysql() {
        return "mysql".equalsIgnoreCase(this.type);
    }
}
