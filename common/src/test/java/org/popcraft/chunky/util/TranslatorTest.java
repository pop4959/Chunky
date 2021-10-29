package org.popcraft.chunky.util;

import org.junit.Test;

import static org.popcraft.chunky.util.Translator.setLanguage;
import static org.popcraft.chunky.util.Translator.translate;

/**
 * This test checks that each translation in every language is parsed without errors.
 */
public class TranslatorTest {
    @Test
    public void testTranslations() {
        testTranslation("bg");
        testTranslation("de");
        testTranslation("en");
        testTranslation("es");
        testTranslation("fr");
        testTranslation("nl");
        testTranslation("no");
        testTranslation("pl");
        testTranslation("pt");
        testTranslation("ru");
        testTranslation("tr");
        testTranslation("vi");
        testTranslation("zh_CN");
        testTranslation("zh_HK");
    }

    private void testTranslation(String language) {
        setLanguage(language);
        translate(TranslationKey.BORDER_DEPENDENCY_UPDATE);
        translate(TranslationKey.BORDER_LOAD_FAILED);
        translate(TranslationKey.BORDER_SAVE_FAILED);
        translate(TranslationKey.COMMAND_NO_PERMISSION);
        translate(TranslationKey.DISABLED);
        translate(TranslationKey.ENABLED);
        translate(TranslationKey.ERROR_VERSION);
        translate(TranslationKey.ERROR_VERSION_SPIGOT);
        translate(TranslationKey.FORMAT_BORDER_ADD, "square", "world", Formatting.number(1), Formatting.number(2), Formatting.number(500));
        translate(TranslationKey.FORMAT_BORDER_BYPASS, "enabled", "pop4959");
        translate(TranslationKey.FORMAT_BORDER_BYPASS_NO_TARGET, "pop4959");
        translate(TranslationKey.FORMAT_BORDER_LIST);
        translate(TranslationKey.FORMAT_BORDER_LIST_BORDER, "world", "square", Formatting.number(1), Formatting.number(2), Formatting.number(500));
        translate(TranslationKey.FORMAT_BORDER_LIST_NONE);
        translate(TranslationKey.FORMAT_BORDER_LOAD, "world");
        translate(TranslationKey.FORMAT_BORDER_NO_BORDER, "world");
        translate(TranslationKey.FORMAT_BORDER_REMOVE, "world");
        translate(TranslationKey.FORMAT_BORDER_WRAP, "enabled", "world");
        translate(TranslationKey.FORMAT_CANCEL, "world");
        translate(TranslationKey.FORMAT_CANCEL_ALL);
        translate(TranslationKey.FORMAT_CANCEL_CONFIRM, "/chunky confirm");
        translate(TranslationKey.FORMAT_CANCEL_NO_TASKS);
        translate(TranslationKey.FORMAT_CENTER, Formatting.number(1), Formatting.number(2));
        translate(TranslationKey.FORMAT_CONFIRM);
        translate(TranslationKey.FORMAT_CONTINUE, "world");
        translate(TranslationKey.FORMAT_CONTINUE_NO_TASKS);
        translate(TranslationKey.FORMAT_TRIM_CONFIRM, "world", translate(TranslationKey.SHAPE_SQUARE), Formatting.number(1), Formatting.number(2), "500", "/chunky confirm");
        translate(TranslationKey.FORMAT_PATTERN, "concentric");
        translate(TranslationKey.FORMAT_PAUSE, "world");
        translate(TranslationKey.FORMAT_PAUSE_NO_TASKS);
        translate(TranslationKey.FORMAT_QUIET, 1);
        translate(TranslationKey.FORMAT_RADII, Formatting.number(500), Formatting.number(1000));
        translate(TranslationKey.FORMAT_RADIUS, Formatting.number(500));
        translate(TranslationKey.FORMAT_RELOAD);
        translate(TranslationKey.FORMAT_RELOAD_TASKS_RUNNING);
        translate(TranslationKey.FORMAT_SHAPE, "square");
        translate(TranslationKey.FORMAT_SILENT, TranslationKey.ENABLED);
        translate(TranslationKey.FORMAT_START, "world", translate(TranslationKey.SHAPE_SQUARE), Formatting.number(1), Formatting.number(2), "500");
        translate(TranslationKey.FORMAT_START_CONFIRM, "/chunky continue", "chunky confirm");
        translate(TranslationKey.FORMAT_START_DISK, "10.5 GB", "20.5 GB", "/chunky confirm");
        translate(TranslationKey.FORMAT_STARTED_ALREADY, "world");
        translate(TranslationKey.FORMAT_WORLD, "world");
        translate(TranslationKey.HELP_BORDER);
        translate(TranslationKey.HELP_CANCEL);
        translate(TranslationKey.HELP_CENTER);
        translate(TranslationKey.HELP_CONTINUE);
        translate(TranslationKey.HELP_CORNERS);
        translate(TranslationKey.HELP_TRIM);
        translate(TranslationKey.HELP_MENU, "");
        translate(TranslationKey.HELP_MORE, "/chunky help 2");
        translate(TranslationKey.HELP_PATTERN);
        translate(TranslationKey.HELP_PAUSE);
        translate(TranslationKey.HELP_QUIET);
        translate(TranslationKey.HELP_RADIUS);
        translate(TranslationKey.HELP_RELOAD);
        translate(TranslationKey.HELP_SHAPE);
        translate(TranslationKey.HELP_SILENT);
        translate(TranslationKey.HELP_SPAWN);
        translate(TranslationKey.HELP_START);
        translate(TranslationKey.HELP_WORLD);
        translate(TranslationKey.HELP_WORLDBORDER);
        translate(TranslationKey.SHAPE_CIRCLE);
        translate(TranslationKey.SHAPE_DIAMOND);
        translate(TranslationKey.SHAPE_ELLIPSE);
        translate(TranslationKey.SHAPE_PENTAGON);
        translate(TranslationKey.SHAPE_RECTANGLE);
        translate(TranslationKey.SHAPE_SQUARE);
        translate(TranslationKey.SHAPE_STAR);
        translate(TranslationKey.SHAPE_TRIANGLE);
        translate(TranslationKey.TASK_TRIM, 5000, "world", String.format("%.3f", 1f));
        translate(TranslationKey.TASK_DONE, "world", 5000, String.format("%.2f", 50f), "0", "01", "15");
        translate(TranslationKey.TASK_STOPPED, "world");
        translate(TranslationKey.TASK_UPDATE, "world", 5000, String.format("%.2f", 50f), "0", "00", "01", String.format("%.1f", 40f), 70, 70);
        translate("prefix");
        translate("null");
    }
}
