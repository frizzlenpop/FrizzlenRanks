package org.frizzlenpop.frizzlenRanks.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.frizzlenpop.frizzlenRanks.FrizzlenRanks;
import org.frizzlenpop.frizzlenRanks.model.Group;
import org.frizzlenpop.frizzlenRanks.model.Track;
import org.frizzlenpop.frizzlenRanks.model.User;
import org.frizzlenpop.frizzlenRanks.model.World;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Manages loading and saving of permission data.
 */
public class DataManager {
    private final FrizzlenRanks plugin;
    private final Map<String, World> worlds;
    private final Map<String, Track> tracks;
    private String selectedWorld;
    
    /**
     * Creates a new DataManager.
     * 
     * @param plugin the plugin instance
     */
    public DataManager(FrizzlenRanks plugin) {
        this.plugin = plugin;
        this.worlds = new HashMap<>();
        this.tracks = new HashMap<>();
        this.selectedWorld = "global";
    }
    
    /**
     * Loads all data from files.
     */
    public void loadAll() {
        // Clear existing data before reloading
        worlds.clear();
        tracks.clear();
        
        // Create data directories if they don't exist
        createDirectories();
        
        // Load global data
        loadWorld("global");
        
        // Load world-specific data if not using global files
        if (!plugin.getConfigManager().useGlobalFiles()) {
            for (org.bukkit.World bukkitWorld : Bukkit.getWorlds()) {
                loadWorld(bukkitWorld.getName());
            }
        }
        
        // Load tracks
        loadTracks();
        
        plugin.getLogger().info("Loaded permission data for " + worlds.size() + " worlds and " + tracks.size() + " tracks");
    }
    
    /**
     * Creates the necessary directories for data storage.
     */
    private void createDirectories() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }
        
        File worldsFolder = new File(dataFolder, "worlds");
        if (!worldsFolder.exists()) {
            worldsFolder.mkdir();
        }
        
        File globalFolder = new File(dataFolder, "global");
        if (!globalFolder.exists()) {
            globalFolder.mkdir();
        }
    }
    
    /**
     * Loads data for a specific world.
     * 
     * @param worldName the name of the world to load
     */
    public void loadWorld(String worldName) {
        String lowerWorldName = worldName.toLowerCase();
        
        // Check if the world is already loaded
        boolean worldExists = worlds.containsKey(lowerWorldName);
        World world;
        
        if (worldExists) {
            // World exists - get it but clear its groups to force reload
            world = worlds.get(lowerWorldName);
            world.clearGroups(); // Force groups to be reloaded
            plugin.getLogger().info("Clearing existing groups for world: " + lowerWorldName);
        } else {
            // Create a new world
            world = new World(lowerWorldName);
            worlds.put(lowerWorldName, world);
            
            if (selectedWorld == null) {
                selectedWorld = lowerWorldName;
            }
        }
        
        // Create the world directory if it doesn't exist
        File worldDir;
        if (lowerWorldName.equalsIgnoreCase("global")) {
            worldDir = new File(plugin.getDataFolder(), "global");
        } else {
            worldDir = new File(plugin.getDataFolder(), "worlds/" + lowerWorldName);
        }
        
        if (!worldDir.exists()) {
            worldDir.mkdirs();
        }
        
        // Log the directory we're looking in
        plugin.getLogger().info("Loading data for world '" + lowerWorldName + "' from: " + worldDir.getAbsolutePath());
        
        // Load groups
        File groupsFile = new File(worldDir, "groups.yml");
        if (groupsFile.exists()) {
            plugin.getLogger().info("Loading groups from: " + groupsFile.getAbsolutePath());
            loadGroups(world, groupsFile);
        } else {
            plugin.getLogger().info("Groups file not found at: " + groupsFile.getAbsolutePath());
            // Apply default groups from the config
            plugin.getConfigManager().applyDefaultGroups(world);
            
            // Save the groups file
            saveGroups(world, groupsFile);
        }
        
        // Load users
        File usersFile = new File(worldDir, "users.yml");
        if (usersFile.exists()) {
            loadUsers(world, usersFile);
        }
    }
    
    /**
     * Loads groups from a file.
     * 
     * @param world the world to load groups into
     * @param file the file to load from
     */
    private void loadGroups(World world, File file) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection groupsSection = config.getConfigurationSection("groups");
        
        if (groupsSection == null) {
            plugin.getLogger().warning("No groups section found in " + file.getPath());
            return;
        }
        
        int groupsLoaded = 0;
        for (String groupName : groupsSection.getKeys(false)) {
            Group group = world.getGroup(groupName);
            ConfigurationSection groupSection = groupsSection.getConfigurationSection(groupName);
            
            if (groupSection == null) {
                continue;
            }
            
            // Load permissions
            List<String> permissions = groupSection.getStringList("permissions");
            for (String permission : permissions) {
                group.addPermission(permission);
            }
            
            // Load inheritance
            List<String> inheritance = groupSection.getStringList("inheritance");
            for (String parent : inheritance) {
                group.addInheritance(parent);
            }
            
            // Load priority
            group.setPriority(groupSection.getInt("priority", 0));
            
            // Load metadata
            ConfigurationSection metaSection = groupSection.getConfigurationSection("meta");
            if (metaSection != null) {
                for (String key : metaSection.getKeys(false)) {
                    group.setMeta(key, metaSection.getString(key));
                }
            }
            
            groupsLoaded++;
            plugin.getLogger().info("Loaded group: " + groupName + " with " + permissions.size() + 
                                   " permissions and prefix: " + group.getMeta("prefix"));
        }
        
        plugin.getLogger().info("Loaded " + groupsLoaded + " groups for world " + world.getName() + " from " + file.getPath());
    }
    
    /**
     * Loads users from a file.
     * 
     * @param world the world to load users into
     * @param file the file to load from
     */
    private void loadUsers(World world, File file) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection usersSection = config.getConfigurationSection("users");
        
        if (usersSection == null) {
            return;
        }
        
        for (String userName : usersSection.getKeys(false)) {
            User user = world.getUser(userName);
            ConfigurationSection userSection = usersSection.getConfigurationSection(userName);
            
            if (userSection == null) {
                continue;
            }
            
            // Load permissions
            List<String> permissions = userSection.getStringList("permissions");
            for (String permission : permissions) {
                user.addPermission(permission);
            }
            
            // Load groups
            List<String> groups = userSection.getStringList("groups");
            for (String group : groups) {
                user.addGroup(group);
            }
            
            // Load metadata
            ConfigurationSection metaSection = userSection.getConfigurationSection("meta");
            if (metaSection != null) {
                for (String key : metaSection.getKeys(false)) {
                    user.setMeta(key, metaSection.getString(key));
                }
            }
        }
    }
    
    /**
     * Loads tracks from the tracks.yml file.
     */
    private void loadTracks() {
        File tracksFile = new File(plugin.getDataFolder(), "tracks.yml");
        
        if (!tracksFile.exists()) {
            createDefaultTracks(tracksFile);
            return;
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(tracksFile);
        ConfigurationSection tracksSection = config.getConfigurationSection("tracks");
        
        if (tracksSection == null) {
            return;
        }
        
        for (String trackName : tracksSection.getKeys(false)) {
            Track track = new Track(trackName);
            List<String> groups = tracksSection.getStringList(trackName);
            
            for (String group : groups) {
                track.addGroup(group);
            }
            
            tracks.put(trackName.toLowerCase(), track);
        }
    }
    
    /**
     * Creates default tracks.
     * 
     * @param file the file to save to
     */
    private void createDefaultTracks(File file) {
        Track staffTrack = new Track("staff");
        staffTrack.addGroup("default");
        staffTrack.addGroup("moderator");
        staffTrack.addGroup("admin");
        
        tracks.put("staff", staffTrack);
        
        saveTracks(file);
    }
    
    /**
     * Saves all data to files.
     */
    public void saveAll() {
        for (World world : worlds.values()) {
            saveWorld(world);
        }
        
        saveTracks(new File(plugin.getDataFolder(), "tracks.yml"));
        
        plugin.getLogger().info("Saved permission data for " + worlds.size() + " worlds and " + tracks.size() + " tracks");
    }
    
    /**
     * Saves data for a specific world.
     * 
     * @param world the world to save
     */
    public void saveWorld(World world) {
        // Determine the directory to save to
        File directory;
        if (world.getName().equalsIgnoreCase("global")) {
            directory = new File(plugin.getDataFolder(), "global");
        } else {
            directory = new File(plugin.getDataFolder(), "worlds/" + world.getName());
        }
        
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        // Save groups
        File groupsFile = new File(directory, "groups.yml");
        saveGroups(world, groupsFile);
        
        // Save users
        File usersFile = new File(directory, "users.yml");
        saveUsers(world, usersFile);
    }
    
    /**
     * Saves groups to a file.
     * 
     * @param world the world to save groups from
     * @param file the file to save to
     */
    private void saveGroups(World world, File file) {
        FileConfiguration config = new YamlConfiguration();
        ConfigurationSection groupsSection = config.createSection("groups");
        
        for (Group group : world.getGroups()) {
            ConfigurationSection groupSection = groupsSection.createSection(group.getName());
            
            // Save permissions
            groupSection.set("permissions", new ArrayList<>(group.getPermissions()));
            
            // Save inheritance
            groupSection.set("inheritance", new ArrayList<>(group.getInheritance()));
            
            // Save priority
            groupSection.set("priority", group.getPriority());
            
            // Save metadata
            ConfigurationSection metaSection = groupSection.createSection("meta");
            for (Map.Entry<String, String> entry : group.getMeta().entrySet()) {
                metaSection.set(entry.getKey(), entry.getValue());
            }
        }
        
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save groups to " + file.getPath(), e);
        }
    }
    
    /**
     * Saves users to a file.
     * 
     * @param world the world to save users from
     * @param file the file to save to
     */
    private void saveUsers(World world, File file) {
        FileConfiguration config = new YamlConfiguration();
        ConfigurationSection usersSection = config.createSection("users");
        
        for (User user : world.getUsers()) {
            ConfigurationSection userSection = usersSection.createSection(user.getName());
            
            // Save permissions
            userSection.set("permissions", new ArrayList<>(user.getPermissions()));
            
            // Save groups
            userSection.set("groups", new ArrayList<>(user.getGroups()));
            
            // Save metadata
            ConfigurationSection metaSection = userSection.createSection("meta");
            for (Map.Entry<String, String> entry : user.getMeta().entrySet()) {
                metaSection.set(entry.getKey(), entry.getValue());
            }
        }
        
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save users to " + file.getPath(), e);
        }
    }
    
    /**
     * Saves tracks to a file.
     * 
     * @param file the file to save to
     */
    private void saveTracks(File file) {
        FileConfiguration config = new YamlConfiguration();
        ConfigurationSection tracksSection = config.createSection("tracks");
        
        for (Track track : tracks.values()) {
            tracksSection.set(track.getName(), track.getGroups());
        }
        
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save tracks to " + file.getPath(), e);
        }
    }
    
    /**
     * Gets a world by name.
     * 
     * @param worldName the name of the world
     * @return the world, or null if not found
     */
    public World getWorld(String worldName) {
        if (plugin.getConfigManager().useGlobalFiles()) {
            return worlds.get("global");
        }
        
        String lowerName = worldName.toLowerCase();
        if (worlds.containsKey(lowerName)) {
            return worlds.get(lowerName);
        }
        
        // If the world doesn't exist, create it
        World world = new World(lowerName);
        worlds.put(lowerName, world);
        return world;
    }
    
    /**
     * Gets a track by name.
     * 
     * @param trackName the name of the track
     * @return the track, or null if not found
     */
    public Track getTrack(String trackName) {
        return tracks.get(trackName.toLowerCase());
    }
    
    /**
     * Gets all tracks.
     * 
     * @return a collection of all tracks
     */
    public Collection<Track> getTracks() {
        return tracks.values();
    }
    
    /**
     * Creates a new track.
     * 
     * @param trackName the name of the track
     * @return the new track
     */
    public Track createTrack(String trackName) {
        Track track = new Track(trackName);
        tracks.put(trackName.toLowerCase(), track);
        return track;
    }
    
    /**
     * Removes a track.
     * 
     * @param trackName the name of the track to remove
     * @return true if the track was found and removed
     */
    public boolean removeTrack(String trackName) {
        return tracks.remove(trackName.toLowerCase()) != null;
    }
    
    /**
     * Gets the currently selected world.
     * 
     * @return the name of the selected world
     */
    public String getSelectedWorld() {
        return selectedWorld;
    }
    
    /**
     * Sets the selected world.
     * 
     * @param worldName the name of the world to select
     */
    public void setSelectedWorld(String worldName) {
        String lowerName = worldName.toLowerCase();
        if (!worlds.containsKey(lowerName)) {
            loadWorld(lowerName);
        }
        selectedWorld = lowerName;
        plugin.getLogger().info("Selected world: " + selectedWorld);
    }
    
    /**
     * Gets the currently selected world object.
     * 
     * @return the selected world object
     */
    public World getSelectedWorldObj() {
        if (!worlds.containsKey(selectedWorld)) {
            loadWorld(selectedWorld);
        }
        return worlds.get(selectedWorld);
    }
    
    /**
     * Synchronizes a user across all worlds if global users is enabled.
     * 
     * @param sourceWorld the world the user was updated in
     * @param userName the name of the user to synchronize
     */
    public void syncUserAcrossWorlds(World sourceWorld, String userName) {
        if (!plugin.getConfigManager().useGlobalUsers()) {
            return;
        }
        
        User sourceUser = sourceWorld.getUser(userName);
        
        for (World targetWorld : worlds.values()) {
            if (targetWorld == sourceWorld) {
                continue;
            }
            
            User targetUser = targetWorld.getUser(userName);
            
            // Sync groups
            targetUser.setGroups(sourceUser.getGroups());
            
            // Sync permissions
            targetUser.clearPermissions();
            for (String permission : sourceUser.getPermissions()) {
                targetUser.addPermission(permission);
            }
            
            // Sync metadata
            targetUser.clearMeta();
            for (Map.Entry<String, String> entry : sourceUser.getMeta().entrySet()) {
                targetUser.setMeta(entry.getKey(), entry.getValue());
            }
            
            // Save the target world
            if (plugin.getConfigManager().autoSave()) {
                saveWorld(targetWorld);
            }
        }
    }
    
    /**
     * Clears all permissions from a user.
     */
    public void clearUserPermissions(User user) {
        user.clearPermissions();
    }
    
    /**
     * Backs up all permission data.
     */
    public void backup() {
        File backupFolder = new File(plugin.getDataFolder(), "backups");
        if (!backupFolder.exists()) {
            backupFolder.mkdir();
        }
        
        String timestamp = String.valueOf(System.currentTimeMillis());
        File backupDir = new File(backupFolder, timestamp);
        backupDir.mkdir();
        
        // Backup global
        File globalDir = new File(plugin.getDataFolder(), "global");
        if (globalDir.exists()) {
            File backupGlobalDir = new File(backupDir, "global");
            backupGlobalDir.mkdir();
            copyFiles(globalDir, backupGlobalDir);
        }
        
        // Backup worlds
        File worldsDir = new File(plugin.getDataFolder(), "worlds");
        if (worldsDir.exists()) {
            File backupWorldsDir = new File(backupDir, "worlds");
            backupWorldsDir.mkdir();
            copyDirectory(worldsDir, backupWorldsDir);
        }
        
        // Backup tracks
        File tracksFile = new File(plugin.getDataFolder(), "tracks.yml");
        if (tracksFile.exists()) {
            try {
                org.bukkit.configuration.file.FileConfiguration config = YamlConfiguration.loadConfiguration(tracksFile);
                config.save(new File(backupDir, "tracks.yml"));
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not backup tracks.yml", e);
            }
        }
        
        plugin.getLogger().info("Created backup in " + backupDir.getPath());
    }
    
    /**
     * Copies files from one directory to another.
     * 
     * @param source the source directory
     * @param destination the destination directory
     */
    private void copyFiles(File source, File destination) {
        if (!source.isDirectory()) {
            return;
        }
        
        File[] files = source.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isFile()) {
                try {
                    FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                    config.save(new File(destination, file.getName()));
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Could not backup " + file.getPath(), e);
                }
            }
        }
    }
    
    /**
     * Copies a directory and its contents.
     * 
     * @param source the source directory
     * @param destination the destination directory
     */
    private void copyDirectory(File source, File destination) {
        if (!source.isDirectory()) {
            return;
        }
        
        File[] files = source.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                File newDir = new File(destination, file.getName());
                newDir.mkdir();
                copyDirectory(file, newDir);
            } else {
                try {
                    FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                    config.save(new File(destination, file.getName()));
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Could not backup " + file.getPath(), e);
                }
            }
        }
    }
    
    /**
     * Force reloads all groups from files, clearing any cached data.
     * This is useful when group files have been manually edited.
     */
    public void forceReloadGroups() {
        plugin.getLogger().info("Force reloading all groups...");
        
        // For each world, reload its groups
        for (World world : worlds.values()) {
            // First clear all existing groups
            world.clearGroups();
            
            // Determine the directory to load from
            File worldDir;
            if (world.getName().equalsIgnoreCase("global")) {
                worldDir = new File(plugin.getDataFolder(), "global");
            } else {
                worldDir = new File(plugin.getDataFolder(), "worlds/" + world.getName());
            }
            
            // Load groups
            File groupsFile = new File(worldDir, "groups.yml");
            if (groupsFile.exists()) {
                plugin.getLogger().info("Reloading groups for world " + world.getName() + " from " + groupsFile.getPath());
                loadGroups(world, groupsFile);
            } else {
                plugin.getLogger().warning("Groups file not found for world " + world.getName() + " at " + groupsFile.getPath());
            }
        }
        
        plugin.getLogger().info("Finished force reloading all groups");
    }
    
    /**
     * Gets all worlds managed by the plugin.
     * 
     * @return a collection of all worlds
     */
    public Collection<World> getWorlds() {
        return Collections.unmodifiableCollection(worlds.values());
    }
} 