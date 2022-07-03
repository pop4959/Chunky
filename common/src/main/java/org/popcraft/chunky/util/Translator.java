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

public final class Translator {
    private static final Map<String, String> fallbackTranslations;
    private static Map<String, String> translations = Collections.emptyMap();

    static {
        fallbackTranslations = load("en");
    }

    private Translator() {
    }

    public static void setLanguage(final String language) {
        translations = load(language);
    }

    public static boolean isValidLanguage(final String language) {
        return Translator.class.getClassLoader().getResource("lang/" + language + ".json") != null;
    }

    public static String translateKey(final String key, final boolean prefixed, final Object... args) {
        final StringBuilder translation = new StringBuilder();
        final String message = translations.getOrDefault(key, fallbackTranslations.getOrDefault(key, "Missing translation"));
        if (prefixed) {
            translation.append("[Chunky] ");
        }
        translation.append(String.format(message, args));
        return translation.toString();
    }

    public static String translate(final String key, final Object... args) {
        return translateKey(key, false, args);
    }

    public static void addCustomTranslation(final String key, final String message) {
        fallbackTranslations.put(key, message);
    }

    private static Map<String, String> load(final String language) {
        final InputStream input = Translator.class.getClassLoader().getResourceAsStream("lang/" + language + ".json");
        if (input != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                final StringBuilder lang = new StringBuilder();
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
