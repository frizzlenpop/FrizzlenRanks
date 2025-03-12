package org.frizzlenpop.frizzlenRanks.vault;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.frizzlenpop.frizzlenRanks.FrizzlenRanks;
import org.frizzlenpop.frizzlenRanks.model.Group;
import org.frizzlenpop.frizzlenRanks.model.User;
import org.frizzlenpop.frizzlenRanks.model.World;

/**
 * Provides integration with Vault's chat API.
 */
public class VaultChatHook extends Chat {
    private final FrizzlenRanks plugin;
    
    /**
     * Creates a new VaultChatHook.
     * 
     * @param plugin the plugin instance
     */
    public VaultChatHook(FrizzlenRanks plugin) {
        super(plugin.getPermissionHook());
        this.plugin = plugin;
    }
    
    @Override
    public String getName() {
        return "FrizzlenRanks";
    }
    
    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }
    
    @Override
    public String getPlayerPrefix(String worldName, String playerName) {
        World world = getWorld(worldName);
        if (!world.hasUser(playerName)) {
            return "";
        }
        
        User user = world.getUser(playerName);
        
        // Check for user-specific prefix
        String prefix = user.getMeta("prefix");
        if (prefix != null) {
            return prefix;
        }
        
        // Check for group prefixes
        for (String groupName : user.getGroups()) {
            if (world.hasGroup(groupName)) {
                Group group = world.getGroup(groupName);
                prefix = group.getMeta("prefix");
                if (prefix != null) {
                    return prefix;
                }
            }
        }
        
        return "";
    }
    
    @Override
    public String getPlayerPrefix(String worldName, OfflinePlayer player) {
        return getPlayerPrefix(worldName, player.getName());
    }
    
    @Override
    public void setPlayerPrefix(String worldName, String playerName, String prefix) {
        World world = getWorld(worldName);
        User user = world.getUser(playerName);
        user.setMeta("prefix", prefix);
        saveIfNeeded();
    }
    
    @Override
    public void setPlayerPrefix(String worldName, OfflinePlayer player, String prefix) {
        setPlayerPrefix(worldName, player.getName(), prefix);
    }
    
    @Override
    public String getPlayerSuffix(String worldName, String playerName) {
        World world = getWorld(worldName);
        if (!world.hasUser(playerName)) {
            return "";
        }
        
        User user = world.getUser(playerName);
        
        // Check for user-specific suffix
        String suffix = user.getMeta("suffix");
        if (suffix != null) {
            return suffix;
        }
        
        // Check for group suffixes
        for (String groupName : user.getGroups()) {
            if (world.hasGroup(groupName)) {
                Group group = world.getGroup(groupName);
                suffix = group.getMeta("suffix");
                if (suffix != null) {
                    return suffix;
                }
            }
        }
        
        return "";
    }
    
    @Override
    public String getPlayerSuffix(String worldName, OfflinePlayer player) {
        return getPlayerSuffix(worldName, player.getName());
    }
    
    @Override
    public void setPlayerSuffix(String worldName, String playerName, String suffix) {
        World world = getWorld(worldName);
        User user = world.getUser(playerName);
        user.setMeta("suffix", suffix);
        saveIfNeeded();
    }
    
    @Override
    public void setPlayerSuffix(String worldName, OfflinePlayer player, String suffix) {
        setPlayerSuffix(worldName, player.getName(), suffix);
    }
    
    @Override
    public String getGroupPrefix(String worldName, String groupName) {
        World world = getWorld(worldName);
        if (!world.hasGroup(groupName)) {
            return "";
        }
        
        Group group = world.getGroup(groupName);
        String prefix = group.getMeta("prefix");
        return prefix != null ? prefix : "";
    }
    
    @Override
    public void setGroupPrefix(String worldName, String groupName, String prefix) {
        World world = getWorld(worldName);
        Group group = world.getGroup(groupName);
        group.setMeta("prefix", prefix);
        saveIfNeeded();
    }
    
    @Override
    public String getGroupSuffix(String worldName, String groupName) {
        World world = getWorld(worldName);
        if (!world.hasGroup(groupName)) {
            return "";
        }
        
        Group group = world.getGroup(groupName);
        String suffix = group.getMeta("suffix");
        return suffix != null ? suffix : "";
    }
    
    @Override
    public void setGroupSuffix(String worldName, String groupName, String suffix) {
        World world = getWorld(worldName);
        Group group = world.getGroup(groupName);
        group.setMeta("suffix", suffix);
        saveIfNeeded();
    }
    
    @Override
    public int getPlayerInfoInteger(String worldName, String playerName, String node, int defaultValue) {
        String value = getPlayerInfoString(worldName, playerName, node, null);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    @Override
    public int getPlayerInfoInteger(String worldName, OfflinePlayer player, String node, int defaultValue) {
        return getPlayerInfoInteger(worldName, player.getName(), node, defaultValue);
    }
    
    @Override
    public void setPlayerInfoInteger(String worldName, String playerName, String node, int value) {
        setPlayerInfoString(worldName, playerName, node, String.valueOf(value));
    }
    
    @Override
    public void setPlayerInfoInteger(String worldName, OfflinePlayer player, String node, int value) {
        setPlayerInfoInteger(worldName, player.getName(), node, value);
    }
    
    @Override
    public int getGroupInfoInteger(String worldName, String groupName, String node, int defaultValue) {
        String value = getGroupInfoString(worldName, groupName, node, null);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    @Override
    public void setGroupInfoInteger(String worldName, String groupName, String node, int value) {
        setGroupInfoString(worldName, groupName, node, String.valueOf(value));
    }
    
    @Override
    public double getPlayerInfoDouble(String worldName, String playerName, String node, double defaultValue) {
        String value = getPlayerInfoString(worldName, playerName, node, null);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    @Override
    public double getPlayerInfoDouble(String worldName, OfflinePlayer player, String node, double defaultValue) {
        return getPlayerInfoDouble(worldName, player.getName(), node, defaultValue);
    }
    
    @Override
    public void setPlayerInfoDouble(String worldName, String playerName, String node, double value) {
        setPlayerInfoString(worldName, playerName, node, String.valueOf(value));
    }
    
    @Override
    public void setPlayerInfoDouble(String worldName, OfflinePlayer player, String node, double value) {
        setPlayerInfoDouble(worldName, player.getName(), node, value);
    }
    
    @Override
    public double getGroupInfoDouble(String worldName, String groupName, String node, double defaultValue) {
        String value = getGroupInfoString(worldName, groupName, node, null);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    @Override
    public void setGroupInfoDouble(String worldName, String groupName, String node, double value) {
        setGroupInfoString(worldName, groupName, node, String.valueOf(value));
    }
    
    @Override
    public boolean getPlayerInfoBoolean(String worldName, String playerName, String node, boolean defaultValue) {
        String value = getPlayerInfoString(worldName, playerName, node, null);
        if (value == null) {
            return defaultValue;
        }
        
        return Boolean.parseBoolean(value);
    }
    
    @Override
    public boolean getPlayerInfoBoolean(String worldName, OfflinePlayer player, String node, boolean defaultValue) {
        return getPlayerInfoBoolean(worldName, player.getName(), node, defaultValue);
    }
    
    @Override
    public void setPlayerInfoBoolean(String worldName, String playerName, String node, boolean value) {
        setPlayerInfoString(worldName, playerName, node, String.valueOf(value));
    }
    
    @Override
    public void setPlayerInfoBoolean(String worldName, OfflinePlayer player, String node, boolean value) {
        setPlayerInfoBoolean(worldName, player.getName(), node, value);
    }
    
    @Override
    public boolean getGroupInfoBoolean(String worldName, String groupName, String node, boolean defaultValue) {
        String value = getGroupInfoString(worldName, groupName, node, null);
        if (value == null) {
            return defaultValue;
        }
        
        return Boolean.parseBoolean(value);
    }
    
    @Override
    public void setGroupInfoBoolean(String worldName, String groupName, String node, boolean value) {
        setGroupInfoString(worldName, groupName, node, String.valueOf(value));
    }
    
    @Override
    public String getPlayerInfoString(String worldName, String playerName, String node, String defaultValue) {
        World world = getWorld(worldName);
        if (!world.hasUser(playerName)) {
            return defaultValue;
        }
        
        User user = world.getUser(playerName);
        String value = user.getMeta(node);
        return value != null ? value : defaultValue;
    }
    
    @Override
    public String getPlayerInfoString(String worldName, OfflinePlayer player, String node, String defaultValue) {
        return getPlayerInfoString(worldName, player.getName(), node, defaultValue);
    }
    
    @Override
    public void setPlayerInfoString(String worldName, String playerName, String node, String value) {
        World world = getWorld(worldName);
        User user = world.getUser(playerName);
        user.setMeta(node, value);
        saveIfNeeded();
    }
    
    @Override
    public void setPlayerInfoString(String worldName, OfflinePlayer player, String node, String value) {
        setPlayerInfoString(worldName, player.getName(), node, value);
    }
    
    @Override
    public String getGroupInfoString(String worldName, String groupName, String node, String defaultValue) {
        World world = getWorld(worldName);
        if (!world.hasGroup(groupName)) {
            return defaultValue;
        }
        
        Group group = world.getGroup(groupName);
        String value = group.getMeta(node);
        return value != null ? value : defaultValue;
    }
    
    @Override
    public void setGroupInfoString(String worldName, String groupName, String node, String value) {
        World world = getWorld(worldName);
        Group group = world.getGroup(groupName);
        group.setMeta(node, value);
        saveIfNeeded();
    }
    
    /**
     * Gets a world by name, using global if configured.
     * 
     * @param worldName the name of the world
     * @return the world object
     */
    private World getWorld(String worldName) {
        if (plugin.getConfigManager().useGlobalFiles()) {
            return plugin.getDataManager().getWorld("global");
        }
        
        if (worldName == null) {
            return plugin.getDataManager().getSelectedWorldObj();
        }
        
        return plugin.getDataManager().getWorld(worldName);
    }
    
    /**
     * Saves data if auto-save is enabled.
     */
    private void saveIfNeeded() {
        if (plugin.getConfigManager().autoSave()) {
            plugin.getDataManager().saveAll();
        }
    }
} 