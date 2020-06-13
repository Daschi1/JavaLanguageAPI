# JavaLanguageAPI
## A language API for java.

```java
LanguageAPI.setLanguageHandler(new LanguageHandler(LanguageHandler.LanguageSaveMode.YAML, "languageAPI", "en_us")); //setup language API with yaml (local)
LanguageAPI.setLanguageHandler(new LanguageHandler(LanguageHandler.LanguageSaveMode.MySQL, "languageAPI", "en_us", "hostname", 3306, "username", "password", "database")); //setup language API with mysql (remote)

LanguageAPI.setLanguage("en_uk"); //change language

LanguageAPI.setValue("key", "value"); //set value for current language (you can also provide a specific language as the final argument)
LanguageAPI.getValue("key"); //get value for current language (you can also provide a specific language as the final argument)
LanguageAPI.hasValue("key"); //check if value exists for current language (you can also provide a specific language as the final argument)
```
