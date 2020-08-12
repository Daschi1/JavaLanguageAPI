package de.daschi.javalanguageapi.api;

import de.daschi.core.MySQL;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import javax.sql.rowset.CachedRowSet;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class LanguageHandler {

    private final LanguageSaveMode languageSaveMode;
    private final String folderPath;
    private LanguageHandler cache;
    private String language;

    public LanguageHandler(final LanguageSaveMode languageSaveMode, final String folderPath, final String language) {
        this(languageSaveMode, folderPath, language, "", -1, "", "", "");
    }

    public LanguageHandler(final LanguageSaveMode languageSaveMode, final String folderPath, final String language, final String hostname, final int port, final String username, final String password, final String database) {
        this.languageSaveMode = languageSaveMode;
        this.folderPath = folderPath.endsWith("/") ? folderPath : folderPath + "/";
        this.language = language;

        if (this.languageSaveMode.equals(LanguageSaveMode.MySQL)) {
            this.setupMySQL(hostname, port, username, password, database);
        }
    }

    private void setupMySQL(final String hostname, final int port, final String username, final String password, final String database) {
        this.cache = new LanguageHandler(LanguageSaveMode.YAML, this.folderPath + "cache/", this.language);
        MySQL.add("JavaLanguageAPI", new MySQL(hostname, port, username, password, database));
        MySQL.autoDisconnect("JavaLanguageAPI", true);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
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
            final YamlFile yamlFile = this.checkYamlFile(language);
            if (this.hasValue(key, language)) {
                return yamlFile.getString(key);
            }
        } else {
            if (this.cache.hasValue(key)) {
                return this.cache.getValue(key);
            } else {
                try {
                    final CachedRowSet cachedRowSet = MySQL.query("JavaLanguageAPI", "SELECT * FROM `" + MySQL.preventSQLInjection("JavaLanguageAPI", language) + "` WHERE `key` = '" + MySQL.preventSQLInjection("JavaLanguageAPI", key) + "';");
                    if (cachedRowSet.next()) {
                        final String value = cachedRowSet.getString("value");
                        this.cache.setValue(key, value);
                        return value;
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

    public void setValue(final String key, final String value, final String language) {
        if (this.languageSaveMode.equals(LanguageSaveMode.YAML)) {
            final File file = new File(this.folderPath + language + ".yml");
            try {
                final YamlFile yamlFile = new YamlFile(file);
                if (!yamlFile.exists()) {
                    yamlFile.createNewFile(false);
                }
                yamlFile.loadWithComments();
                yamlFile.set(key, value);
                yamlFile.saveWithComments();
            } catch (final IOException | InvalidConfigurationException exception) {
                throw new LanguageException("Could not save the yaml config '" + file.getAbsolutePath() + "'.", exception);
            }
        } else {
            MySQL.update("JavaLanguageAPI", "CREATE TABLE IF NOT EXISTS `" + MySQL.preventSQLInjection("JavaLanguageAPI", language) + "` " +
                    "(" +
                    "`key` text," +
                    "`value` text," +
                    "UNIQUE(`key`)" +
                    ");");
            MySQL.update("JavaLanguageAPI", "INSERT INTO `" + MySQL.preventSQLInjection("JavaLanguageAPI", language) + "` (`key`, `value`) VALUES ('" + MySQL.preventSQLInjection("JavaLanguageAPI", key) + "', '" + MySQL.preventSQLInjection("JavaLanguageAPI", value) + "') ON DUPLICATE KEY UPDATE `value` = '" + MySQL.preventSQLInjection("JavaLanguageAPI", value) + "';");
        }
    }

    public boolean hasValue(final String key) {
        return this.hasValue(key, this.language);
    }

    public boolean hasValue(final String key, final String language) {
        if (this.languageSaveMode.equals(LanguageSaveMode.YAML)) {
            final YamlFile yamlFile = this.checkYamlFile(language);
            return yamlFile.contains(key);
        } else {
            try {
                final CachedRowSet cachedRowSet = MySQL.query("JavaLanguageAPI", "SELECT * FROM `" + MySQL.preventSQLInjection("JavaLanguageAPI", language) + "` WHERE `key` = '" + MySQL.preventSQLInjection("JavaLanguageAPI", key) + "';");
                return cachedRowSet.next();
            } catch (final SQLException exception) {
                throw new LanguageException("Could not execute a query to the mysql.", exception);
            }
        }
    }

    private YamlFile checkYamlFile(final String language) {
        final YamlFile yamlFile = new YamlFile(new File(this.folderPath + language + ".yml"));
        try {
            if (!yamlFile.exists()) {
                yamlFile.createNewFile(false);
            }
            yamlFile.loadWithComments();
        } catch (final InvalidConfigurationException | IOException e) {
            e.printStackTrace();
        }
        return yamlFile;
    }

    public void removeValue(final String key) {
        this.setValue(key, this.language);
    }

    public void removeValue(final String key, final String language) {
        if (this.languageSaveMode.equals(LanguageSaveMode.YAML)) {
            final File file = new File(this.folderPath + language + ".yml");
            try {
                final YamlFile yamlFile = new YamlFile(file);
                if (!yamlFile.exists()) {
                    yamlFile.createNewFile(false);
                }
                yamlFile.loadWithComments();
                if (this.hasValue(key, language)) {
                    yamlFile.remove(key);
                }
                yamlFile.saveWithComments();
            } catch (final IOException | InvalidConfigurationException exception) {
                throw new LanguageException("Could not save the yaml config '" + file.getAbsolutePath() + "'.", exception);
            }
        } else {
            if (this.hasValue(key, language)) {
                MySQL.update("JavaLanguageAPI", "DELETE FROM `" + MySQL.preventSQLInjection("JavaLanguageAPI", language) + "` WHERE `key` = '" + MySQL.preventSQLInjection("JavaLanguageAPI", key) + "';");
            }
        }
    }

    public LanguageSaveMode getLanguageSaveMode() {
        return this.languageSaveMode;
    }

    public String getFolderPath() {
        return this.folderPath;
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
