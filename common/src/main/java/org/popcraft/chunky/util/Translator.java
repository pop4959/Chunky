package org.popcraft.chunky.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Translator {
    private static final Map<String, String> fallbackTranslations;
    private static Map<String, String> translations = Collections.emptyMap();

    static {
        fallbackTranslations = load("en");
    }

    private Translator() {
    }

    public static void setLanguage(String language) {
        translations = load(language);
    }

    public static boolean isValidLanguage(String language) {
        return Translator.class.getClassLoader().getResource("lang/" + language + ".json") != null;
    }

    public static String translateKey(String key, boolean prefixed, Object... args) {
        StringBuilder translation = new StringBuilder();
        String message = translations.getOrDefault(key, fallbackTranslations.getOrDefault(key, "Missing translation"));
        if (prefixed) {
            translation.append("[Chunky] ");
        }
        translation.append(String.format(message, args));
        return translation.toString();
    }

    public static String translate(String key, Object... args) {
        return translateKey(key, false, args);
    }

    public static void addCustomTranslation(String key, String message) {
        fallbackTranslations.put(key, message);
    }

    private static Map<String, String> load(String language) {
        InputStream input = Translator.class.getClassLoader().getResourceAsStream("lang/" + language + ".json");
        if (input != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                StringBuilder lang = new StringBuilder();
                String s;
                while ((s = reader.readLine()) != null) {
                    lang.append(s);
                }
                return new Gson().fromJson(lang.toString(), new TypeToken<HashMap<String, String>>() {
                }.getType());
            } catch (Exception ignored) {
            }
        }
        return Collections.emptyMap();
    }
}
