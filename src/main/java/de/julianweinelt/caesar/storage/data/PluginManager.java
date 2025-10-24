package de.julianweinelt.caesar.storage.data;

import de.julianweinelt.caesar.CaesarEndpoint;
import de.julianweinelt.caesar.storage.MySQL;
import de.julianweinelt.caesar.web.PluginEntry;
import de.julianweinelt.caesar.web.PluginState;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PluginManager {
    @Getter
    private final List<PluginEntry> plugins = new ArrayList<>();

    public static PluginManager getInstance() {
        return CaesarEndpoint.getInstance().getPluginManager();
    }

    public void getData() {
        plugins.clear();
        plugins.addAll(MySQL.getInstance().getPlugins());
    }

    public void createPlugin(PluginEntry pluginEntry) {
        plugins.add(pluginEntry);

        pluginEntry.setWaitingForAppoval(true);
        MySQL.getInstance().setPluginWaitingApproval(pluginEntry.getUniqueId(), true);
        MySQL.getInstance().importPlugin(pluginEntry);
    }

    public PluginEntry getPlugin(String name) {
        for (PluginEntry plugin : plugins) {
            if (plugin.getName().equals(name)) return plugin;
        }
        return null;
    }
    public PluginEntry getPlugin(UUID uniqueId) {
        for (PluginEntry plugin : plugins) {
            if (plugin.getUniqueId().equals(uniqueId)) return plugin;
        }
        return null;
    }

    public boolean updatePluginState(UUID plugin, PluginState state) {
        getPlugin(plugin).setState(state);
        return MySQL.getInstance().updatePluginState(plugin, state) != 0;
    }
}