package org.frizzlenpop.frizzlenRanks;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.frizzlenpop.frizzlenRanks.commands.*;
import org.frizzlenpop.frizzlenRanks.config.ConfigManager;
import org.frizzlenpop.frizzlenRanks.data.DataManager;
import org.frizzlenpop.frizzlenRanks.listeners.PlayerListener;
import org.frizzlenpop.frizzlenRanks.model.Group;
import org.frizzlenpop.frizzlenRanks.model.User;
import org.frizzlenpop.frizzlenRanks.tasks.TemporaryPermissionCleanupTask;
import org.frizzlenpop.frizzlenRanks.vault.VaultChatHook;
import org.frizzlenpop.frizzlenRanks.vault.VaultPermissionHook;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class FrizzlenRanks extends JavaPlugin {
    private static FrizzlenRanks instance;
    private ConfigManager configManager;
    private DataManager dataManager;
    private VaultPermissionHook permissionHook;
    private VaultChatHook chatHook;
    private final Logger logger = getLogger();
    
    // Map to store player permission attachments to prevent garbage collection
    private final java.util.Map<String, org.bukkit.permissions.PermissionAttachment> playerAttachments = new java.util.HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize managers
        configManager = new ConfigManager(this);
        dataManager = new DataManager(this);
        
        // Load data
        dataManager.loadAll();
        
        // Setup Vault hooks
        setupVaultHooks();
        
        // Register commands
        registerCommands();
        
        // Register listeners
        registerListeners();
        
        // Schedule tasks
        scheduleTasks();
        
        logger.info("FrizzlenRanks has been enabled!");
    }
    
    /**
     * Schedules various tasks for the plugin.
     */
    private void scheduleTasks() {
        // Update tab display for all online players (for sorting)
        Bukkit.getScheduler().runTaskLater(this, () -> {
            logger.info("Updating tab display for all online players...");
            for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                PlayerListener tabUpdater = new PlayerListener(this);
                tabUpdater.updatePlayerTabDisplay(player);
            }
        }, 20L); // Run after 1 second to ensure all plugins are loaded
        
        // Schedule a recurring task to refresh the tab list sorting periodically
        // This ensures our sorting persists even if other plugins modify the scoreboard
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                PlayerListener tabUpdater = new PlayerListener(this);
                tabUpdater.ensurePlayerTabSorting(player); // Only check/fix the tab sorting without changing the display name
            }
        }, 100L, 200L); // Run every 10 seconds (200 ticks) after an initial 5-second delay (100 ticks)
        
        // Schedule temporary permission/group cleanup task
        // This removes expired temporary permissions and groups
        new TemporaryPermissionCleanupTask(this).runTaskTimer(this, 300L, 1200L); // Run every minute (1200 ticks) after an initial 15-second delay (300 ticks)
    }

    @Override
    public void onDisable() {
        // Save all data
        dataManager.saveAll();
        
        logger.info("FrizzlenRanks has been disabled!");
    }
    
    private void setupVaultHooks() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            logger.severe("Vault plugin not found! Disabling FrizzlenRanks...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Register permissions service
        permissionHook = new VaultPermissionHook(this);
        getServer().getServicesManager().register(Permission.class, permissionHook, this, ServicePriority.Highest);
        logger.info("Registered Vault Permissions hook");
        
        // Register chat service
        chatHook = new VaultChatHook(this);
        getServer().getServicesManager().register(Chat.class, chatHook, this, ServicePriority.Highest);
        logger.info("Registered Vault Chat hook");
    }
    
    /**
     * Refreshes the Vault hooks after data is reloaded.
     * This ensures that Vault permissions and chat services are updated.
     */
    public void refreshVaultHooks() {
        // Unregister existing services
        getServer().getServicesManager().unregisterAll(this);
        
        // Re-register permissions service
        getServer().getServicesManager().register(Permission.class, permissionHook, this, ServicePriority.Highest);
        
        // Re-register chat service
        getServer().getServicesManager().register(Chat.class, chatHook, this, ServicePriority.Highest);
        
        logger.info("Refreshed Vault Permissions and Chat hooks");
    }
    
    /**
     * Resets Vault's permission cache for a specific player.
     * This ensures their permissions are properly updated after changes.
     * 
     * @param playerName the name of the player to update
     */
    public void resetPlayerPermissionCache(String playerName) {
        org.bukkit.entity.Player player = Bukkit.getPlayer(playerName);
        if (player != null && player.isOnline()) {
            logger.info("Resetting permission cache for player: " + playerName);
            
            try {
                // Remove existing attachment from our storage if it exists
                if (playerAttachments.containsKey(playerName)) {
                    org.bukkit.permissions.PermissionAttachment oldAttachment = playerAttachments.get(playerName);
                    player.removeAttachment(oldAttachment);
                    playerAttachments.remove(playerName);
                    logger.info("Removed previous permission attachment for: " + playerName);
                }
                
                // Clear any other existing permission attachments for this plugin
                player.getEffectivePermissions().forEach(attachment -> {
                    if (attachment.getAttachment() != null && attachment.getAttachment().getPlugin() == this) {
                        player.removeAttachment(attachment.getAttachment());
                    }
                });
                
                // Create a single attachment for all permissions
                org.bukkit.permissions.PermissionAttachment attachment = player.addAttachment(this);
                
                // CRUCIAL: Store the attachment in our map to prevent garbage collection
                playerAttachments.put(playerName, attachment);
                
                // Method 2: Reapply player's group permissions
                String worldName = player.getWorld().getName();
                org.frizzlenpop.frizzlenRanks.model.World pluginWorld = dataManager.getWorld(worldName);
                User user = pluginWorld.getUser(playerName);
                
                // Log groups for debugging
                logger.info("Player " + playerName + " has groups: " + String.join(", ", user.getGroups()));
                
                // For each group, make sure its permissions are refreshed
                for (String groupName : user.getGroups()) {
                    if (pluginWorld.hasGroup(groupName)) {
                        Group group = pluginWorld.getGroup(groupName);
                        logger.info("Group " + groupName + " has " + group.getPermissions().size() + " permissions and prefix: " + group.getMeta("prefix"));
                        
                        // Log all permissions for debugging
                        for (String permission : group.getPermissions()) {
                            logger.info("  Adding permission from group " + groupName + ": " + permission);
                            attachment.setPermission(permission, true);
                        }
                        
                        // Also add inherited permissions from parent groups
                        addInheritedPermissions(pluginWorld, group, attachment, new ArrayList<>());
                    }
                }
                
                // Apply any direct user permissions
                for (String permission : user.getPermissions()) {
                    logger.info("  Adding direct user permission: " + permission);
                    attachment.setPermission(permission, true);
                }
                
                // Force a recalculation of permissions
                player.recalculatePermissions();
                
                logger.info("Successfully reset permissions for player: " + playerName);
                
            } catch (Exception e) {
                logger.log(java.util.logging.Level.SEVERE, "Error resetting permissions for " + playerName + ": " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Recursively adds permissions from parent groups to avoid inheritance issues.
     * 
     * @param world the world containing the groups
     * @param group the current group to process
     * @param attachment the permission attachment to add permissions to
     * @param processedGroups list of groups already processed to prevent infinite recursion
     */
    private void addInheritedPermissions(org.frizzlenpop.frizzlenRanks.model.World world, Group group, 
                                         org.bukkit.permissions.PermissionAttachment attachment, 
                                         List<String> processedGroups) {
        // Prevent infinite recursion
        if (processedGroups.contains(group.getName())) {
            return;
        }
        
        // Mark this group as processed
        processedGroups.add(group.getName());
        
        // Process each parent group first (process inheritance before this group's own permissions)
        // This ensures that higher priority permissions from the current group override inherited ones
        for (String parentName : group.getInheritance()) {
            if (world.hasGroup(parentName)) {
                Group parentGroup = world.getGroup(parentName);
                logger.info("Processing inherited permissions from group: " + parentName + " for group: " + group.getName());
                
                // Recursively process the parent's parents first
                addInheritedPermissions(world, parentGroup, attachment, processedGroups);
                
                // Then add all permissions from the parent group
                for (String permission : parentGroup.getPermissions()) {
                    // Skip negated permissions for logging clarity
                    if (!permission.startsWith("-")) {
                        logger.info("  Adding inherited permission from group " + parentName + ": " + permission);
                    }
                    attachment.setPermission(permission, true);
                }
            }
        }
    }
    
    private void registerCommands() {
        getCommand("user").setExecutor(new UserCommand(this));
        getCommand("group").setExecutor(new GroupCommand(this));
        getCommand("world").setExecutor(new WorldCommand(this));
        getCommand("promote").setExecutor(new PromoteCommand(this));
        getCommand("demote").setExecutor(new DemoteCommand(this));
        getCommand("setgroup").setExecutor(new SetGroupCommand(this));
        getCommand("frizzlenranks").setExecutor(new MainCommand(this));
        getCommand("perms").setExecutor(new PermissionsCommand(this));
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }
    
    public static FrizzlenRanks getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DataManager getDataManager() {
        return dataManager;
    }
    
    public VaultPermissionHook getPermissionHook() {
        return permissionHook;
    }
    
    public VaultChatHook getChatHook() {
        return chatHook;
    }

    // Add cleanup for permissions when a player quits the server
    public void cleanupPlayerPermissions(String playerName) {
        if (playerAttachments.containsKey(playerName)) {
            logger.info("Cleaning up permission attachments for player: " + playerName);
            playerAttachments.remove(playerName);
        }
    }
}
