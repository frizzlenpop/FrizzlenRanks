package org.frizzlenpop.frizzlenRanks.vault;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenRanks.FrizzlenRanks;
import org.frizzlenpop.frizzlenRanks.model.Group;
import org.frizzlenpop.frizzlenRanks.model.User;
import org.frizzlenpop.frizzlenRanks.model.World;

/**
 * Provides integration with Vault's permission API.
 */
public class VaultPermissionHook extends Permission {
    private final FrizzlenRanks plugin;
    
    /**
     * Creates a new VaultPermissionHook.
     * 
     * @param plugin the plugin instance
     */
    public VaultPermissionHook(FrizzlenRanks plugin) {
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
    public boolean hasSuperPermsCompat() {
        return true;
    }
    
    @Override
    public boolean hasGroupSupport() {
        return true;
    }
    
    @Override
    public boolean playerHas(String worldName, String playerName, String permission) {
        World world = getWorld(worldName);
        return world.hasPermission(playerName, permission);
    }
    
    @Override
    public boolean playerHas(String worldName, OfflinePlayer player, String permission) {
        return playerHas(worldName, player.getName(), permission);
    }
    
    @Override
    public boolean playerAdd(String worldName, String playerName, String permission) {
        World world = getWorld(worldName);
        User user = world.getUser(playerName);
        user.addPermission(permission);
        saveIfNeeded();
        return true;
    }
    
    @Override
    public boolean playerAdd(String worldName, OfflinePlayer player, String permission) {
        return playerAdd(worldName, player.getName(), permission);
    }
    
    @Override
    public boolean playerRemove(String worldName, String playerName, String permission) {
        World world = getWorld(worldName);
        User user = world.getUser(playerName);
        user.removePermission(permission);
        saveIfNeeded();
        return true;
    }
    
    @Override
    public boolean playerRemove(String worldName, OfflinePlayer player, String permission) {
        return playerRemove(worldName, player.getName(), permission);
    }
    
    @Override
    public boolean groupHas(String worldName, String groupName, String permission) {
        World world = getWorld(worldName);
        if (!world.hasGroup(groupName)) {
            return false;
        }
        
        Group group = world.getGroup(groupName);
        return group.hasPermission(permission);
    }
    
    @Override
    public boolean groupAdd(String worldName, String groupName, String permission) {
        World world = getWorld(worldName);
        Group group = world.getGroup(groupName);
        group.addPermission(permission);
        saveIfNeeded();
        return true;
    }
    
    @Override
    public boolean groupRemove(String worldName, String groupName, String permission) {
        World world = getWorld(worldName);
        if (!world.hasGroup(groupName)) {
            return false;
        }
        
        Group group = world.getGroup(groupName);
        group.removePermission(permission);
        saveIfNeeded();
        return true;
    }
    
    @Override
    public boolean playerInGroup(String worldName, String playerName, String groupName) {
        World world = getWorld(worldName);
        if (!world.hasUser(playerName)) {
            return false;
        }
        
        User user = world.getUser(playerName);
        return user.inGroup(groupName);
    }
    
    @Override
    public boolean playerInGroup(String worldName, OfflinePlayer player, String groupName) {
        return playerInGroup(worldName, player.getName(), groupName);
    }
    
    @Override
    public boolean playerAddGroup(String worldName, String playerName, String groupName) {
        World world = getWorld(worldName);
        User user = world.getUser(playerName);
        user.addGroup(groupName);
        saveIfNeeded();
        return true;
    }
    
    @Override
    public boolean playerAddGroup(String worldName, OfflinePlayer player, String groupName) {
        return playerAddGroup(worldName, player.getName(), groupName);
    }
    
    @Override
    public boolean playerRemoveGroup(String worldName, String playerName, String groupName) {
        World world = getWorld(worldName);
        if (!world.hasUser(playerName)) {
            return false;
        }
        
        User user = world.getUser(playerName);
        user.removeGroup(groupName);
        saveIfNeeded();
        return true;
    }
    
    @Override
    public boolean playerRemoveGroup(String worldName, OfflinePlayer player, String groupName) {
        return playerRemoveGroup(worldName, player.getName(), groupName);
    }
    
    @Override
    public String[] getPlayerGroups(String worldName, String playerName) {
        World world = getWorld(worldName);
        if (!world.hasUser(playerName)) {
            return new String[0];
        }
        
        User user = world.getUser(playerName);
        return user.getGroups().toArray(new String[0]);
    }
    
    @Override
    public String[] getPlayerGroups(String worldName, OfflinePlayer player) {
        return getPlayerGroups(worldName, player.getName());
    }
    
    @Override
    public String getPrimaryGroup(String worldName, String playerName) {
        String[] groups = getPlayerGroups(worldName, playerName);
        if (groups.length == 0) {
            return null;
        }
        
        // Return the first group as the primary group
        return groups[0];
    }
    
    @Override
    public String getPrimaryGroup(String worldName, OfflinePlayer player) {
        return getPrimaryGroup(worldName, player.getName());
    }
    
    @Override
    public String[] getGroups() {
        World world = plugin.getDataManager().getSelectedWorldObj();
        return world.getGroups().stream()
                .map(Group::getName)
                .toArray(String[]::new);
    }
    
    public boolean hasPlayerInfo(String worldName, String playerName, String node) {
        World world = getWorld(worldName);
        if (!world.hasUser(playerName)) {
            return false;
        }
        
        User user = world.getUser(playerName);
        return user.getMeta(node) != null;
    }
    
    public boolean hasPlayerInfo(String worldName, OfflinePlayer player, String node) {
        return hasPlayerInfo(worldName, player.getName(), node);
    }
    
    public String getPlayerInfoString(String worldName, String playerName, String node, String defaultValue) {
        World world = getWorld(worldName);
        if (!world.hasUser(playerName)) {
            return defaultValue;
        }
        
        User user = world.getUser(playerName);
        String value = user.getMeta(node);
        return value != null ? value : defaultValue;
    }
    
    public String getPlayerInfoString(String worldName, OfflinePlayer player, String node, String defaultValue) {
        return getPlayerInfoString(worldName, player.getName(), node, defaultValue);
    }
    
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
    
    public int getPlayerInfoInteger(String worldName, OfflinePlayer player, String node, int defaultValue) {
        return getPlayerInfoInteger(worldName, player.getName(), node, defaultValue);
    }
    
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
    
    public double getPlayerInfoDouble(String worldName, OfflinePlayer player, String node, double defaultValue) {
        return getPlayerInfoDouble(worldName, player.getName(), node, defaultValue);
    }
    
    public boolean getPlayerInfoBoolean(String worldName, String playerName, String node, boolean defaultValue) {
        String value = getPlayerInfoString(worldName, playerName, node, null);
        if (value == null) {
            return defaultValue;
        }
        
        return Boolean.parseBoolean(value);
    }
    
    public boolean getPlayerInfoBoolean(String worldName, OfflinePlayer player, String node, boolean defaultValue) {
        return getPlayerInfoBoolean(worldName, player.getName(), node, defaultValue);
    }
    
    public void setPlayerInfo(String worldName, String playerName, String node, Object value) {
        World world = getWorld(worldName);
        User user = world.getUser(playerName);
        user.setMeta(node, value != null ? value.toString() : null);
        saveIfNeeded();
    }
    
    public void setPlayerInfo(String worldName, OfflinePlayer player, String node, Object value) {
        setPlayerInfo(worldName, player.getName(), node, value);
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