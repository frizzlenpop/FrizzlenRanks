package org.frizzlenpop.frizzlenRanks.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.frizzlenpop.frizzlenRanks.FrizzlenRanks;
import org.frizzlenpop.frizzlenRanks.model.Group;
import org.frizzlenpop.frizzlenRanks.model.World;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConfigManager {
    private final FrizzlenRanks plugin;
    private FileConfiguration config;
    private File configFile;
    
    public ConfigManager(FrizzlenRanks plugin) {
        this.plugin = plugin;
        setup();
    }
    
    private void setup() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        
        configFile = new File(plugin.getDataFolder(), "config.yml");
        
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                plugin.saveResource("config.yml", true);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create config.yml file: " + e.getMessage());
            }
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        setDefaults();
    }
    
    private void setDefaults() {
        config.addDefault("use-global-files", false);
        config.addDefault("use-global-users", true);
        config.addDefault("auto-save", true);
        config.addDefault("track-type", "single");
        config.addDefault("chat-format", "{prefix}&f{name}{suffix}&f: {message}");
        config.options().copyDefaults(true);
        save();
    }
    
    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config.yml file: " + e.getMessage());
        }
    }
    
    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
    
    public boolean useGlobalFiles() {
        return config.getBoolean("use-global-files");
    }
    
    public boolean useGlobalUsers() {
        return config.getBoolean("use-global-users");
    }
    
    public boolean autoSave() {
        return config.getBoolean("auto-save");
    }
    
    public String getTrackType() {
        return config.getString("track-type");
    }
    
    public String getChatFormat() {
        return config.getString("chat-format", "{prefix}&f{name}{suffix}&f: {message}");
    }
    
    /**
     * Applies default groups defined in the config to a world
     *
     * @param world the world to apply default groups to
     */
    public void applyDefaultGroups(World world) {
        ConfigurationSection defaultGroups = config.getConfigurationSection("default-groups");
        if (defaultGroups == null) {
            return;
        }
        
        for (String groupName : defaultGroups.getKeys(false)) {
            // Get or create the group
            Group group = world.getGroup(groupName);
            
            // Set prefix and suffix
            String prefix = defaultGroups.getString(groupName + ".prefix");
            if (prefix != null) {
                group.setMeta("prefix", prefix);
            }
            
            String suffix = defaultGroups.getString(groupName + ".suffix");
            if (suffix != null) {
                group.setMeta("suffix", suffix);
            }
            
            // Add permissions
            List<String> permissions = defaultGroups.getStringList(groupName + ".permissions");
            for (String permission : permissions) {
                group.addPermission(permission);
            }
        }
    }
} 