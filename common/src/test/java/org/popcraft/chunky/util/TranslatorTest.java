package org.popcraft.chunky.util;

import org.junit.Test;

import static org.popcraft.chunky.util.Translator.*;

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
        testTranslation("pl");
        testTranslation("pt");
        testTranslation("ru");
        testTranslation("tr");
        testTranslation("zh_CN");
    }

    private void testTranslation(String language) {
        setLanguage(language);
        translate("command_no_permission");
        translate("disabled");
        translate("enabled");
        translate("error_version");
        translate("error_version_spigot");
        translate("format_cancel", "world");
        translate("format_cancel_all");
        translate("format_cancel_confirm", "/chunky confirm");
        translate("format_cancel_no_tasks");
        translate("format_center", 1, 2);
        translate("format_confirm");
        translate("format_continue", "world");
        translate("format_continue_no_tasks");
        translate("format_delete_confirm", "world", "square", 1, 2, "500", "/chunky confirm");
        translate("format_pattern", "concentric");
        translate("format_pause", "world");
        translate("format_pause_no_tasks");
        translate("format_quiet", 1);
        translate("format_radii", 500, 1000);
        translate("format_radius", 500);
        translate("format_reload");
        translate("format_reload_tasks_running");
        translate("format_shape", "square");
        translate("format_silent", "enabled");
        translate("format_start", "world", 1, 2, 500);
        translate("format_start_confirm", "/chunky continue", "chunky confirm");
        translate("format_start_disk", "10.5 GB", "20.5 GB", "/chunky confirm");
        translate("format_started_already", "world");
        translate("format_world", "world");
        translate("help_cancel");
        translate("help_center");
        translate("help_continue");
        translate("help_corners");
        translate("help_delete");
        translate("help_menu", "");
        translate("help_more", "/chunky help 2");
        translate("help_pattern");
        translate("help_pause");
        translate("help_quiet");
        translate("help_radius");
        translate("help_reload");
        translate("help_shape");
        translate("help_silent");
        translate("help_spawn");
        translate("help_start");
        translate("help_world");
        translate("help_worldborder");
        translate("prefix");
        translate("shape_circle");
        translate("shape_diamond");
        translate("shape_ellipse");
        translate("shape_pentagon");
        translate("shape_rectangle");
        translate("shape_square");
        translate("shape_star");
        translate("shape_triangle");
        translate("task_delete", 5000, "world", String.format("%.3f", 1f));
        translate("task_done", "world", 5000, String.format("%.2f", 50f), "0", "01", "15");
        translate("task_stopped", "world");
        translate("task_update", "world", 5000, String.format("%.2f", 50f), "0", "00", "01", String.format("%.1f", 40f), 70, 70);
        translate("null");
    }
}
