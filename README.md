# JavaLanguageAPI
## A language API for java with YAML or MySQL.

[![](https://jitpack.io/v/Daschi1/JavaLanguageAPI.svg)](https://jitpack.io/#Daschi1/JavaLanguageAPI)

```java
LanguageAPI.setLanguageHandler(new LanguageHandler(LanguageHandler.LanguageSaveMode.YAML, "languageAPI", "en_us")); //setup language API with yaml (local)
LanguageAPI.setLanguageHandler(new LanguageHandler(LanguageHandler.LanguageSaveMode.MySQL, "languageAPI", "en_us", "hostname", 3306, "username", "password", "database")); //setup language API with mysql (remote)

LanguageAPI.setLanguage("en_uk"); //change language

LanguageAPI.setValue("key", "value"); //set value for current language (you can also provide a specific language as the final argument)
LanguageAPI.getValue("key"); //get value for current language (you can also provide a specific language as the final argument)
LanguageAPI.hasValue("key"); //check if value exists for current language (you can also provide a specific language as the final argument)
```

Gradle:

```gradle
repositories {
      maven { url 'https://jitpack.io' }
}

dependencies {
    compile 'com.github.Daschi1:JavaLanguageAPI:v1.0.0a'
}
  
```

Maven:
    
```maven
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
  
<dependency>
    <groupId>com.github.Daschi1</groupId>
    <artifactId>JavaLanguageAPI</artifactId>
    <version>v1.0.0a</version>
</dependency>
```
