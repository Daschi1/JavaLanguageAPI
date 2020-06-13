package de.daschi.javalanguageapi.api;

public class LanguageAPI {

    private static LanguageHandler languageHandler;

    static {
        LanguageAPI.languageHandler = new LanguageHandler(LanguageHandler.LanguageSaveMode.YAML, "JavaLanguageAPI", "en_us");
    }

    public static LanguageHandler getLanguageHandler() {
        return LanguageAPI.languageHandler;
    }

    public static void setLanguageHandler(final LanguageHandler languageHandler) {
        LanguageAPI.languageHandler = languageHandler;
    }

    public static void setLanguage(final String language) {
        LanguageAPI.languageHandler.setLanguage(language);
    }

    public static String getValue(final String key) {
        return LanguageAPI.languageHandler.getValue(key);
    }

    public static String getValue(final String key, final String language) {
        return LanguageAPI.languageHandler.getValue(key, language);
    }

    public static void setValue(final String key, final String value) {
        LanguageAPI.languageHandler.setValue(key, value);
    }

    public static void setValue(final String key, final String value, final String language) {
        LanguageAPI.languageHandler.setValue(key, value, language);
    }

    public static boolean hasValue(final String key) {
        return LanguageAPI.languageHandler.hasValue(key);
    }

    public static boolean hasValue(final String key, final String language) {
        return LanguageAPI.languageHandler.hasValue(key, language);
    }

}
