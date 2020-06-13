package de.daschi.javalanguageapi.api;

import de.daschi.javalanguageapi.mysql.MySQL;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class LanguageHandler {

    private final LanguageSaveMode languageSaveMode;
    private final String folderPath;
    private LanguageHandler cache;
    private MySQL mySQL;
    private String language;

    public LanguageHandler(final LanguageSaveMode languageSaveMode, final String folderPath, final String language) {
        this(languageSaveMode, folderPath, language, "", -1, "", "", "");
    }

    public LanguageHandler(final LanguageSaveMode languageSaveMode, final String folderPath, final String language, final String hostname, final int port, final String username, final String password, final String database) {
        this.languageSaveMode = languageSaveMode;
        this.folderPath = folderPath.endsWith("/") ? folderPath : folderPath + "/";
        this.language = language;

        if (this.languageSaveMode.equals(LanguageSaveMode.MySQL)) {
            try {
                this.setupMySQL(hostname, port, username, password, database);
            } catch (final SQLException | ClassNotFoundException exception) {
                throw new LanguageException("Could not initialise the mysql connection.", exception);
            }
        }
    }

    private void setupMySQL(final String hostname, final int port, final String username, final String password, final String database) throws SQLException, ClassNotFoundException {
        this.cache = new LanguageHandler(LanguageSaveMode.YAML, this.folderPath + "cache/", this.language);
        this.mySQL = new MySQL(hostname, String.valueOf(port), database, username, password);
        this.mySQL.openConnection();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                this.mySQL.closeConnection();
            } catch (final SQLException exception) {
                throw new LanguageException("Could not close the mysql connection.", exception);
            }

            final File cacheFolder = new File(this.cache.getFolderPath());
            for (final String s : Objects.requireNonNull(cacheFolder.list())) {
                final File currentFile = new File(cacheFolder.getPath(), s);
                if (!currentFile.delete()) {
                    currentFile.deleteOnExit();
                }
            }
        }));
    }

    public String getValue(final String key) {
        return this.getValue(key, this.language);
    }

    public String getValue(final String key, final String language) {
        if (this.languageSaveMode.equals(LanguageSaveMode.YAML)) {
            final YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(new File(this.folderPath + language + ".yml"));
            if (yamlConfiguration.contains(key)) {
                return yamlConfiguration.getString(key);
            }
        } else {
            if (this.cache.hasValue(key)) {
                return this.cache.getValue(key);
            } else {
                try {
                    final ResultSet resultSet = this.mySQL.executeQuery("SELECT * FROM `" + language + "` WHERE `key` = '" + key + "';");
                    if (resultSet.next()) {
                        final String value = resultSet.getString("value");
                        this.cache.setValue(key, value);
                        resultSet.close();
                        return value;
                    } else {
                        resultSet.close();
                    }
                } catch (final SQLException exception) {
                    throw new LanguageException("Could not execute a query to the mysql.", exception);
                }
            }
        }
        throw new LanguageException("The key '" + key + "' for the language '" + language + "' is not defined.");
    }

    public void setValue(final String key, final String value) {
        this.setValue(key, value, this.language);
    }

    public void setValue(String key, String value, final String language) {
        if (this.languageSaveMode.equals(LanguageSaveMode.YAML)) {
            final File yamlFile = new File(this.folderPath + language + ".yml");
            try {
                final YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(yamlFile);
                yamlConfiguration.set(key, value);
                yamlConfiguration.save(yamlFile);
            } catch (final IOException exception) {
                throw new LanguageException("Could not save the yaml config '" + yamlFile.getAbsolutePath() + "'.", exception);
            }
        } else {
            value = value.replaceAll("[']", "\\\\'");
            key = key.replaceAll("[']", "\\\\'");
            try {
                this.mySQL.executeUpdate("CREATE TABLE IF NOT EXISTS `" + language + "` " +
                        "(" +
                        "`key` text," +
                        "`value` text," +
                        "UNIQUE(`key`)" +
                        ");");
                this.mySQL.executeUpdate("INSERT INTO `" + language + "` (`key`, `value`) VALUES ('" + key + "', '" + value + "') ON DUPLICATE KEY UPDATE `value` = '" + value + "';");
            } catch (final SQLException exception) {
                throw new LanguageException("Could not execute an update to the mysql.", exception);
            }
        }
    }

    public boolean hasValue(final String key) {
        return this.hasValue(key, this.language);
    }

    public boolean hasValue(final String key, final String language) {
        if (this.languageSaveMode.equals(LanguageSaveMode.YAML)) {
            final YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(new File(this.folderPath + language + ".yml"));
            return yamlConfiguration.contains(key);
        } else {
            try {
                final ResultSet resultSet = this.mySQL.executeQuery("SELECT * FROM `" + language + "` WHERE `key` = '" + key + "';");
                final boolean exists = resultSet.next();
                resultSet.close();
                return exists;
            } catch (final SQLException exception) {
                throw new LanguageException("Could not execute a query to the mysql.", exception);
            }
        }
    }

    public LanguageSaveMode getLanguageSaveMode() {
        return this.languageSaveMode;
    }

    public String getFolderPath() {
        return this.folderPath;
    }

    public MySQL getMySQL() {
        return this.mySQL;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(final String language) {
        this.language = language;
        if (this.cache != null) {
            this.cache.setLanguage(language);
        }
    }

    public enum LanguageSaveMode {
        YAML,
        MySQL
    }
}
