package io.github.jochyoua.griefpreventionantienter;

import io.github.jochyoua.griefpreventionantienter.events.PlayerEntryEvent;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public class GPAE extends JavaPlugin {

    @Override
    public void onEnable() {

        // Saves the default config we have included with the Jar file, must be named config.yml to use this method
        saveDefaultConfig();

        // Registering PlayerMoveEvent
        getServer().getPluginManager().registerEvents(new PlayerEntryEvent(this), this);

    }

    /**
     * Polishes a string with custom variables and makes sure that a string has proper colors
     * <p>
     * This method will replace all custom variables with the proper information,
     * You can make more custom variables via the config or via the replacement string like so:
     * polishString(String, "variable:cool string")
     *
     * @param str         the string to be polished
     * @param replacement any custom variables other than ones defined in config
     */
    public String polishString(String str, String replacement) {
        List<String> replacements = getConfig().getStringList("settings.custom variables");
        if (replacement != null) {
            if (replacement.contains("|"))
                replacements.addAll(Arrays.asList(replacement.split("\\|")));
            else
                replacements.add(replacement);
        }
        for (String variables : replacements) {
            String[] strings = variables.split(":");
            str = str.replaceAll("(?i)\\{" + strings[0].trim() + "}", strings[1].trim());
        }
        return ChatColor.translateAlternateColorCodes('&', str);
    }
}
