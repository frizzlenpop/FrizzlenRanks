package org.frizzlenpop.frizzlenRanks.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.frizzlenpop.frizzlenRanks.FrizzlenRanks;
import org.frizzlenpop.frizzlenRanks.model.User;
import org.frizzlenpop.frizzlenRanks.model.World;
import org.frizzlenpop.frizzlenRanks.model.Group;
import org.frizzlenpop.frizzlenRanks.listeners.PlayerListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the main plugin commands.
 */
public class MainCommand implements CommandExecutor, TabCompleter {
    private final FrizzlenRanks plugin;
    private final List<String> subCommands = Arrays.asList(
            "reload", "save", "backup", "helpme", "info", "version", "fix", "forceglobal", "refreshgroups", "checkperms", "refreshpermissions", "testperm"
    );
    
    /**
     * Creates a new MainCommand.
     * 
     * @param plugin the plugin instance
     */
    public MainCommand(FrizzlenRanks plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("frizzlenranks.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (args.length == 0) {
            displayHelp(sender);
            return true;
        }
        
        String action = args[0].toLowerCase();
        
        switch (action) {
            case "reload":
                // Reload configuration and data
                plugin.getConfigManager().reload();
                plugin.getDataManager().loadAll();
                
                // Refresh Vault hooks to ensure they have the latest data
                plugin.refreshVaultHooks();
                
                // Update permissions for all online players
                sender.sendMessage(ChatColor.GREEN + "Updating permissions for all online players...");
                for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                    // Ensure the player exists in the data after reload
                    String worldName = player.getWorld().getName();
                    World world = plugin.getDataManager().getWorld(worldName);
                    User user = world.getUser(player.getName());
                    
                    // If the user has no groups, add them to the default group
                    if (user.getGroups().isEmpty()) {
                        user.addGroup("default");
                        
                        // Save if auto-save is enabled
                        if (plugin.getConfigManager().autoSave()) {
                            plugin.getDataManager().saveWorld(world);
                            plugin.getDataManager().syncUserAcrossWorlds(world, player.getName());
                        }
                    }
                    
                    // Update tab display
                    PlayerListener playerListener = new PlayerListener(plugin);
                    playerListener.updatePlayerTabDisplay(player);
                    
                    // Log for debugging
                    plugin.getLogger().info("Updated permissions for player: " + player.getName() + 
                                          " (Groups: " + String.join(", ", user.getGroups()) + ")");
                }
                
                sender.sendMessage(ChatColor.GREEN + "FrizzlenRanks configuration and data reloaded.");
                break;
                
            case "save":
                // Save data
                plugin.getDataManager().saveAll();
                sender.sendMessage(ChatColor.GREEN + "FrizzlenRanks data saved.");
                break;
                
            case "backup":
                // Backup data
                plugin.getDataManager().backup();
                sender.sendMessage(ChatColor.GREEN + "FrizzlenRanks data backed up.");
                break;
                
            case "helpme":
                // Create default files
                plugin.getDataManager().loadAll();
                sender.sendMessage(ChatColor.GREEN + "Default permission files created.");
                break;
                
            case "info":
                // Display plugin info
                displayInfo(sender);
                break;
                
            case "version":
                // Display plugin version
                sender.sendMessage(ChatColor.GREEN + "FrizzlenRanks version: " + plugin.getDescription().getVersion());
                break;
                
            case "fix":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /fr fix <username>");
                    return true;
                }
                
                String userName = args[1];
                fixUserPermissions(sender, userName, args);
                break;
                
            case "forceglobal":
                // Force global configuration settings
                plugin.getConfigManager().getConfig().set("use-global-files", true);
                plugin.getConfigManager().getConfig().set("use-global-users", true);
                plugin.getConfigManager().save();
                
                // Reload data with new settings
                plugin.getDataManager().loadAll();
                
                // Refresh Vault hooks
                plugin.refreshVaultHooks();
                
                // Notify sender
                sender.sendMessage(ChatColor.GREEN + "Forced global permission configuration:");
                sender.sendMessage(ChatColor.GREEN + "- Global Files: " + ChatColor.WHITE + "Enabled");
                sender.sendMessage(ChatColor.GREEN + "- Global Users: " + ChatColor.WHITE + "Enabled");
                sender.sendMessage(ChatColor.GREEN + "Settings have been saved and data reloaded.");
                
                // Update all online players
                sender.sendMessage(ChatColor.GREEN + "Updating all online players...");
                for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                    String worldName = player.getWorld().getName();
                    World world = plugin.getDataManager().getWorld(worldName);
                    User user = world.getUser(player.getName());
                    plugin.getDataManager().syncUserAcrossWorlds(world, player.getName());
                    PlayerListener playerListener = new PlayerListener(plugin);
                    playerListener.updatePlayerTabDisplay(player);
                    sender.sendMessage(ChatColor.GREEN + "Updated player: " + player.getName());
                }
                break;
                
            case "refreshgroups":
                // Force reload all groups from files
                sender.sendMessage(ChatColor.GREEN + "Force reloading all groups from files...");
                
                // Force reload groups
                plugin.getDataManager().forceReloadGroups();
                
                // Refresh Vault hooks
                plugin.refreshVaultHooks();
                
                // Update all online players
                sender.sendMessage(ChatColor.GREEN + "Updating permissions for all online players...");
                for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                    // Reset permission cache and update tab display
                    plugin.resetPlayerPermissionCache(player.getName());
                    PlayerListener playerListener = new PlayerListener(plugin);
                    playerListener.updatePlayerTabDisplay(player);
                    
                    // Print the user's current groups
                    World world = plugin.getDataManager().getWorld(player.getWorld().getName());
                    User user = world.getUser(player.getName());
                    sender.sendMessage(ChatColor.GREEN + "Updated player: " + player.getName() + 
                                     " - Groups: " + ChatColor.WHITE + String.join(", ", user.getGroups()));
                }
                
                sender.sendMessage(ChatColor.GREEN + "All groups have been refreshed from files.");
                break;
                
            case "checkperms":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /fr checkperms <username> [permission]");
                    return true;
                }
                
                String checkPlayerName = args[1];
                org.bukkit.entity.Player checkPlayer = Bukkit.getPlayer(checkPlayerName);
                
                if (checkPlayer == null || !checkPlayer.isOnline()) {
                    sender.sendMessage(ChatColor.RED + "Player " + checkPlayerName + " is not online.");
                    return true;
                }
                
                // Check for a specific permission if provided
                if (args.length > 2) {
                    String checkPerm = args[2];
                    boolean hasPerm = checkPlayer.hasPermission(checkPerm);
                    sender.sendMessage(ChatColor.GREEN + "Player " + checkPlayerName + " " + 
                                     (hasPerm ? "HAS" : "DOES NOT HAVE") + " permission: " + checkPerm);
                } else {
                    // List all effective permissions
                    sender.sendMessage(ChatColor.GREEN + "===== Permissions for " + checkPlayerName + " =====");
                    
                    // First, show what groups they're in
                    String worldName = checkPlayer.getWorld().getName();
                    org.frizzlenpop.frizzlenRanks.model.World world = plugin.getDataManager().getWorld(worldName);
                    org.frizzlenpop.frizzlenRanks.model.User user = world.getUser(checkPlayerName);
                    
                    sender.sendMessage(ChatColor.GREEN + "Groups: " + ChatColor.WHITE + 
                                     String.join(", ", user.getGroups()));
                    
                    // Show all effective permissions
                    sender.sendMessage(ChatColor.GREEN + "Effective Permissions:");
                    
                    int count = 0;
                    for (org.bukkit.permissions.PermissionAttachmentInfo permission : checkPlayer.getEffectivePermissions()) {
                        if (permission.getValue()) {
                            sender.sendMessage(ChatColor.WHITE + "- " + permission.getPermission() + 
                                              " (from: " + (permission.getAttachment() != null ? 
                                                          permission.getAttachment().getPlugin().getName() : "default") + ")");
                            count++;
                            
                            // Limit the output to avoid spamming
                            if (count >= 30) {
                                sender.sendMessage(ChatColor.YELLOW + "... and more. Use '/fr checkperms " + 
                                                 checkPlayerName + " <permission>' to check a specific permission.");
                                break;
                            }
                        }
                    }
                    
                    if (count == 0) {
                        sender.sendMessage(ChatColor.RED + "No permissions found!");
                    }
                }
                
                break;
                
            case "refreshpermissions":
                if (args.length < 2) {
                    // Refresh for all online players
                    sender.sendMessage(ChatColor.GREEN + "Refreshing permissions for all online players...");
                    for (org.bukkit.entity.Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        plugin.resetPlayerPermissionCache(onlinePlayer.getName());
                        PlayerListener playerListener = new PlayerListener(plugin);
                        playerListener.updatePlayerTabDisplay(onlinePlayer);
                        sender.sendMessage(ChatColor.GREEN + "Refreshed permissions for: " + onlinePlayer.getName());
                    }
                } else {
                    // Refresh for specific player
                    String playerName = args[1];
                    org.bukkit.entity.Player targetPlayer = Bukkit.getPlayer(playerName);
                    
                    if (targetPlayer == null || !targetPlayer.isOnline()) {
                        sender.sendMessage(ChatColor.RED + "Player " + playerName + " is not online.");
                        return true;
                    }
                    
                    sender.sendMessage(ChatColor.GREEN + "Refreshing permissions for " + playerName + "...");
                    plugin.resetPlayerPermissionCache(playerName);
                    
                    // Update tab display
                    PlayerListener playerListener = new PlayerListener(plugin);
                    playerListener.updatePlayerTabDisplay(targetPlayer);
                    
                    // Show the player's current groups
                    org.frizzlenpop.frizzlenRanks.model.World world = plugin.getDataManager().getWorld(targetPlayer.getWorld().getName());
                    org.frizzlenpop.frizzlenRanks.model.User user = world.getUser(playerName);
                    sender.sendMessage(ChatColor.GREEN + "Player's groups: " + ChatColor.WHITE + String.join(", ", user.getGroups()));
                    
                    // Count effective permissions
                    int permCount = 0;
                    for (org.bukkit.permissions.PermissionAttachmentInfo perm : targetPlayer.getEffectivePermissions()) {
                        if (perm.getValue()) permCount++;
                    }
                    
                    sender.sendMessage(ChatColor.GREEN + "Player has " + permCount + " active permissions.");
                    sender.sendMessage(ChatColor.GREEN + "Use '/fr checkperms " + playerName + "' to view them.");
                }
                break;
                
            case "testperm":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /fr testperm <player> <permission>");
                    return true;
                }
                
                String testPlayerName = args[1];
                String testPermission = args[2];
                org.bukkit.entity.Player testPlayer = Bukkit.getPlayer(testPlayerName);
                
                if (testPlayer == null || !testPlayer.isOnline()) {
                    sender.sendMessage(ChatColor.RED + "Player " + testPlayerName + " is not online.");
                    return true;
                }
                
                // Test the permission using superPerms
                boolean hasPerm = testPlayer.hasPermission(testPermission);
                
                sender.sendMessage(ChatColor.GREEN + "=== Permission Test ===");
                sender.sendMessage(ChatColor.GREEN + "Player: " + ChatColor.WHITE + testPlayerName);
                sender.sendMessage(ChatColor.GREEN + "Permission: " + ChatColor.WHITE + testPermission);
                sender.sendMessage(ChatColor.GREEN + "Result: " + (hasPerm ? 
                    ChatColor.GREEN + "GRANTED" : ChatColor.RED + "DENIED"));
                
                // Check if there are any wildcard permissions that might be granting this
                if (hasPerm) {
                    sender.sendMessage(ChatColor.GREEN + "Permission is granted directly.");
                } else {
                    // Try to find parent permissions that might grant it
                    sender.sendMessage(ChatColor.YELLOW + "Checking for parent permissions that might grant it...");
                    
                    // Generate possible parent wildcard patterns
                    String[] parts = testPermission.split("\\.");
                    StringBuilder builder = new StringBuilder();
                    
                    for (int i = 0; i < parts.length - 1; i++) {
                        builder.append(parts[i]).append(".");
                        String wildcard = builder.toString() + "*";
                        
                        boolean hasWildcard = testPlayer.hasPermission(wildcard);
                        sender.sendMessage(ChatColor.YELLOW + "Wildcard " + wildcard + ": " + 
                            (hasWildcard ? ChatColor.GREEN + "GRANTED" : ChatColor.RED + "DENIED"));
                    }
                }
                
                // Show the player's groups
                World playerWorld = plugin.getDataManager().getWorld(testPlayer.getWorld().getName());
                User user = playerWorld.getUser(testPlayerName);
                sender.sendMessage(ChatColor.GREEN + "Player's Groups: " + 
                    ChatColor.WHITE + String.join(", ", user.getGroups()));
                
                // Check if any of the player's groups have the permission directly
                boolean foundInGroup = false;
                for (String groupName : user.getGroups()) {
                    if (playerWorld.hasGroup(groupName)) {
                        Group group = playerWorld.getGroup(groupName);
                        if (group.hasPermission(testPermission)) {
                            sender.sendMessage(ChatColor.GREEN + "Permission found in group: " + groupName);
                            foundInGroup = true;
                        } else {
                            // Check for inherited permissions too
                            boolean inherited = checkInheritedPermission(playerWorld, group, testPermission, new ArrayList<>());
                            if (inherited) {
                                sender.sendMessage(ChatColor.GREEN + "Permission inherited from a parent of group: " + groupName);
                                foundInGroup = true;
                            }
                        }
                    }
                }
                
                if (!foundInGroup) {
                    sender.sendMessage(ChatColor.RED + "Permission not found in any of the player's groups or their inheritance.");
                }
                
                break;
                
            default:
                sender.sendMessage(ChatColor.RED + "Unknown action: " + action);
                displayHelp(sender);
                return true;
        }
        
        return true;
    }
    
    /**
     * Fixes user permissions by syncing across all worlds and updating cache.
     * 
     * @param sender the command sender
     * @param userName the username to fix
     * @param args the command arguments
     */
    private void fixUserPermissions(CommandSender sender, String userName, String[] args) {
        // Enable global users if not already enabled
        if (!plugin.getConfigManager().getConfig().getBoolean("use-global-users")) {
            plugin.getConfigManager().getConfig().set("use-global-users", true);
            plugin.getConfigManager().save();
            sender.sendMessage(ChatColor.GREEN + "Enabled global users to fix permissions.");
        }
        
        // Make sure the user exists in each world and has the correct group
        for (org.bukkit.World bukkitWorld : Bukkit.getWorlds()) {
            String worldName = bukkitWorld.getName();
            sender.sendMessage(ChatColor.GREEN + "Checking world: " + ChatColor.WHITE + worldName);
            
            // Get or create the world
            World world = plugin.getDataManager().getWorld(worldName);
            User user = world.getUser(userName);
            
            // Show current groups
            sender.sendMessage(ChatColor.GREEN + "  Current groups: " + ChatColor.WHITE + String.join(", ", user.getGroups()));
            
            // Add admin group if requested specifically
            if (args.length > 2 && args[2].equalsIgnoreCase("admin")) {
                user.addGroup("admin");
                sender.sendMessage(ChatColor.GREEN + "  Added to admin group.");
            }
            
            // Save the world
            plugin.getDataManager().saveWorld(world);
        }
        
        // Sync the user across all worlds
        World sourceWorld = plugin.getDataManager().getWorld("global");
        plugin.getDataManager().syncUserAcrossWorlds(sourceWorld, userName);
        
        // Update tab display name if player is online
        org.bukkit.entity.Player onlinePlayer = Bukkit.getPlayer(userName);
        if (onlinePlayer != null && onlinePlayer.isOnline()) {
            PlayerListener playerListener = new PlayerListener(plugin);
            playerListener.updatePlayerTabDisplay(onlinePlayer);
        }
        
        sender.sendMessage(ChatColor.GREEN + "User " + userName + " permissions have been fixed across all worlds.");
        sender.sendMessage(ChatColor.GREEN + "Please reconnect to the server for changes to take effect.");
    }
    
    /**
     * Displays help information.
     * 
     * @param sender the command sender
     */
    private void displayHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== FrizzlenRanks Help ===");
        sender.sendMessage(ChatColor.GREEN + "/fr reload" + ChatColor.WHITE + " - Reload configuration and data");
        sender.sendMessage(ChatColor.GREEN + "/fr save" + ChatColor.WHITE + " - Save data");
        sender.sendMessage(ChatColor.GREEN + "/fr backup" + ChatColor.WHITE + " - Backup data");
        sender.sendMessage(ChatColor.GREEN + "/fr helpme" + ChatColor.WHITE + " - Create default files");
        sender.sendMessage(ChatColor.GREEN + "/fr info" + ChatColor.WHITE + " - Display plugin info");
        sender.sendMessage(ChatColor.GREEN + "/fr version" + ChatColor.WHITE + " - Display plugin version");
        sender.sendMessage(ChatColor.GREEN + "/fr fix <username>" + ChatColor.WHITE + " - Fix permissions for a user");
        sender.sendMessage(ChatColor.GREEN + "/fr forceglobal" + ChatColor.WHITE + " - Force global permissions across all worlds");
        sender.sendMessage(ChatColor.GREEN + "/fr refreshgroups" + ChatColor.WHITE + " - Force reload all groups from files");
        sender.sendMessage(ChatColor.GREEN + "/fr checkperms <player> [permission]" + ChatColor.WHITE + " - Check a player's permissions");
        sender.sendMessage(ChatColor.GREEN + "/fr refreshpermissions [player]" + ChatColor.WHITE + " - Refresh permissions for a player or all players");
        sender.sendMessage(ChatColor.GREEN + "/fr testperm <player> <permission>" + ChatColor.WHITE + " - Test if a player has a specific permission");
        sender.sendMessage(ChatColor.GREEN + "/user <username> [action] [arguments]" + ChatColor.WHITE + " - Manage users");
        sender.sendMessage(ChatColor.GREEN + "/group <groupname> [action] [arguments]" + ChatColor.WHITE + " - Manage groups");
        sender.sendMessage(ChatColor.GREEN + "/world <worldname>" + ChatColor.WHITE + " - Select a world");
        sender.sendMessage(ChatColor.GREEN + "/promote <username> [track]" + ChatColor.WHITE + " - Promote a user");
        sender.sendMessage(ChatColor.GREEN + "/demote <username> [track]" + ChatColor.WHITE + " - Demote a user");
        sender.sendMessage(ChatColor.GREEN + "/setgroup <username> <groupname>" + ChatColor.WHITE + " - Set a user's group");
    }
    
    /**
     * Displays plugin information.
     * 
     * @param sender the command sender
     */
    private void displayInfo(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== FrizzlenRanks Info ===");
        sender.sendMessage(ChatColor.GREEN + "Version: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GREEN + "Author: " + ChatColor.WHITE + String.join(", ", plugin.getDescription().getAuthors()));
        sender.sendMessage(ChatColor.GREEN + "Description: " + ChatColor.WHITE + plugin.getDescription().getDescription());
        sender.sendMessage(ChatColor.GREEN + "Selected World: " + ChatColor.WHITE + plugin.getDataManager().getSelectedWorld());
        sender.sendMessage(ChatColor.GREEN + "Auto-Save: " + ChatColor.WHITE + plugin.getConfigManager().autoSave());
        sender.sendMessage(ChatColor.GREEN + "Track Type: " + ChatColor.WHITE + plugin.getConfigManager().getTrackType());
        sender.sendMessage(ChatColor.GREEN + "Use Global Files: " + ChatColor.WHITE + plugin.getConfigManager().useGlobalFiles());
        sender.sendMessage(ChatColor.GREEN + "Use Global Users: " + ChatColor.WHITE + plugin.getConfigManager().useGlobalUsers());
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("frizzlenranks.admin")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            // Return subcommands
            return subCommands.stream()
                    .filter(subCmd -> subCmd.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Handle subcommand tab completion
            String subCommand = args[0].toLowerCase();
            
            if (subCommand.equals("fix") || subCommand.equals("checkperms") || 
                subCommand.equals("refreshpermissions") || subCommand.equals("testperm")) {
                // Return online player names for player-based commands
                return null; // Let Bukkit handle player name completion
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            
            if (subCommand.equals("checkperms") || subCommand.equals("testperm")) {
                // For checkperms/testperm [player] [permission], suggest common permissions
                List<String> commonPermissions = Arrays.asList(
                    "frizzlenranks.admin",
                    "frizzlenranks.user",
                    "essentials.home",
                    "essentials.tpa",
                    "bukkit.command.",
                    "minecraft.command."
                );
                
                return commonPermissions.stream()
                        .filter(perm -> perm.startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Recursively checks if a group has a permission through inheritance.
     * 
     * @param world the world containing the groups
     * @param group the group to check
     * @param permission the permission to check for
     * @param checked list of already checked groups to prevent infinite recursion
     * @return true if the permission is found in any parent group
     */
    private boolean checkInheritedPermission(World world, Group group, String permission, List<String> checked) {
        // Prevent infinite recursion
        if (checked.contains(group.getName())) {
            return false;
        }
        
        // Mark this group as checked
        checked.add(group.getName());
        
        // First check if this group has the permission directly
        if (group.hasPermission(permission)) {
            return true;
        }
        
        // Then check all parent groups
        for (String parentName : group.getInheritance()) {
            if (world.hasGroup(parentName)) {
                Group parentGroup = world.getGroup(parentName);
                if (checkInheritedPermission(world, parentGroup, permission, checked)) {
                    return true;
                }
            }
        }
        
        return false;
    }
} 