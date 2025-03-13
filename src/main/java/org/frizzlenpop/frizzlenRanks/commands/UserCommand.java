package org.frizzlenpop.frizzlenRanks.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenRanks.FrizzlenRanks;
import org.frizzlenpop.frizzlenRanks.listeners.PlayerListener;
import org.frizzlenpop.frizzlenRanks.model.User;
// Using fully qualified name in code instead of import conflict with org.bukkit.World

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles the /user command.
 */
public class UserCommand implements CommandExecutor, TabCompleter {
    private final FrizzlenRanks plugin;
    private final List<String> subCommands = Arrays.asList(
            "addperm", "removeperm", "listperms", "addgroup", "removegroup", "setgroup", "listgroups", "meta", "info", "world",
            "addtempgroup", "removetempgroup", "listtempgroups", "addtempperm", "removetempperm", "listtempperm"
    );
    
    /**
     * Creates a new UserCommand.
     * 
     * @param plugin the plugin instance
     */
    public UserCommand(FrizzlenRanks plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("frizzlenranks.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /user <username> [action] [arguments]");
            return true;
        }
        
        String userName = args[0];
        org.frizzlenpop.frizzlenRanks.model.World world = plugin.getDataManager().getSelectedWorldObj();
        User user = world.getUser(userName);
        
        if (args.length == 1) {
            // Display user info
            sender.sendMessage(ChatColor.GREEN + "User: " + ChatColor.WHITE + user.getName());
            sender.sendMessage(ChatColor.GREEN + "World: " + ChatColor.WHITE + world.getName());
            sender.sendMessage(ChatColor.GREEN + "Groups: " + ChatColor.WHITE + String.join(", ", user.getGroups()));
            sender.sendMessage(ChatColor.GREEN + "Permissions: " + ChatColor.WHITE + user.getPermissions().size() + " permissions");
            sender.sendMessage(ChatColor.GREEN + "Metadata: " + ChatColor.WHITE + user.getMeta().size() + " entries");
            return true;
        }
        
        String action = args[1].toLowerCase();
        
        switch (action) {
            case "addperm":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /user " + userName + " addperm <permission>");
                    return true;
                }
                
                String permission = args[2];
                user.addPermission(permission);
                sender.sendMessage(ChatColor.GREEN + "Added permission " + permission + " to user " + userName);
                break;
                
            case "removeperm":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /user " + userName + " removeperm <permission>");
                    return true;
                }
                
                permission = args[2];
                user.removePermission(permission);
                sender.sendMessage(ChatColor.GREEN + "Removed permission " + permission + " from user " + userName);
                break;
                
            case "listperms":
                sender.sendMessage(ChatColor.GREEN + "Permissions for user " + userName + ":");
                for (String perm : user.getPermissions()) {
                    sender.sendMessage(ChatColor.WHITE + "- " + perm);
                }
                break;
                
            case "addgroup":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /user " + userName + " addgroup <group>");
                    return true;
                }
                
                String groupName = args[2];
                user.addGroup(groupName);
                sender.sendMessage(ChatColor.GREEN + "Added user " + userName + " to group " + groupName);
                break;
                
            case "removegroup":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /user " + userName + " removegroup <group>");
                    return true;
                }
                
                groupName = args[2];
                user.removeGroup(groupName);
                sender.sendMessage(ChatColor.GREEN + "Removed user " + userName + " from group " + groupName);
                break;
                
            case "setgroup":
                return handleSetGroup(sender, args, userName);
                
            case "listgroups":
                sender.sendMessage(ChatColor.GREEN + "Groups for user " + userName + ":");
                for (String group : user.getGroups()) {
                    sender.sendMessage(ChatColor.WHITE + "- " + group);
                }
                break;
                
            case "meta":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /user " + userName + " meta <key> [value]");
                    return true;
                }
                
                String key = args[2];
                
                if (args.length == 3) {
                    // Display meta value
                    String value = user.getMeta(key);
                    if (value != null) {
                        sender.sendMessage(ChatColor.GREEN + "Meta " + key + " for user " + userName + ": " + ChatColor.WHITE + value);
                    } else {
                        sender.sendMessage(ChatColor.RED + "User " + userName + " does not have meta " + key);
                    }
                } else {
                    // Set meta value
                    String value = args[3];
                    user.setMeta(key, value);
                    sender.sendMessage(ChatColor.GREEN + "Set meta " + key + " to " + value + " for user " + userName);
                }
                break;
                
            case "info":
                sender.sendMessage(ChatColor.GREEN + "User: " + ChatColor.WHITE + user.getName());
                sender.sendMessage(ChatColor.GREEN + "World: " + ChatColor.WHITE + world.getName());
                sender.sendMessage(ChatColor.GREEN + "Groups: " + ChatColor.WHITE + String.join(", ", user.getGroups()));
                sender.sendMessage(ChatColor.GREEN + "Permissions: " + ChatColor.WHITE + user.getPermissions().size() + " permissions");
                sender.sendMessage(ChatColor.GREEN + "Metadata: " + ChatColor.WHITE + user.getMeta().size() + " entries");
                break;
                
            case "world":
                if (args.length < 3) {
                    // Display current world for user
                    sender.sendMessage(ChatColor.GREEN + "Current world for " + userName + ": " + ChatColor.WHITE + world.getName());
                    return true;
                }
                
                // Change the selected world for this command
                String worldName = args[2];
                world = plugin.getDataManager().getWorld(worldName);
                user = world.getUser(userName);
                sender.sendMessage(ChatColor.GREEN + "Selected world " + worldName + " for user " + userName);
                break;
                
            case "addtempgroup":
                return handleAddTempGroup(sender, args, userName);
                
            case "removetempgroup":
                return handleRemoveTempGroup(sender, args, userName);
                
            case "listtempgroups":
                return handleListTempGroups(sender, userName);
                
            case "addtempperm":
                return handleAddTempPermission(sender, args, userName);
                
            case "removetempperm":
                return handleRemoveTempPermission(sender, args, userName);
                
            case "listtempperm":
                return handleListTempPermissions(sender, userName);
                
            default:
                sender.sendMessage(ChatColor.RED + "Unknown action: " + action);
                sender.sendMessage(ChatColor.RED + "Available actions: " + String.join(", ", subCommands));
                return true;
        }
        
        // Save if auto-save is enabled
        if (plugin.getConfigManager().autoSave()) {
            plugin.getDataManager().saveWorld(world);
            
            // Sync user across worlds if global users is enabled
            plugin.getDataManager().syncUserAcrossWorlds(world, userName);
            
            // Update player tab display if they're online
            updatePlayerTabDisplay(userName);
        }
        
        // Force immediate update for group changes, even if auto-save is disabled
        if (args.length > 1 && (args[1].equals("addgroup") || args[1].equals("removegroup") || args[1].equals("setgroup"))) {
            // Always save group changes immediately
            plugin.getDataManager().saveWorld(world);
            
            // Always sync group changes across worlds
            plugin.getDataManager().syncUserAcrossWorlds(world, userName);
            
            // Always update tab display for group changes
            updatePlayerTabDisplay(userName);
            
            // Log confirmation for debugging
            plugin.getLogger().info("Forced update of user " + userName + " after group change: " + args[1]);
        }
        
        return true;
    }
    
    /**
     * Updates the tab display name of a player.
     * 
     * @param playerName the name of the player
     */
    private void updatePlayerTabDisplay(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player != null && player.isOnline()) {
            // Create a new PlayerListener instance and use it to update the tab display
            PlayerListener playerListener = new PlayerListener(plugin);
            playerListener.ensurePlayerTabSorting(player);
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("frizzlenranks.admin")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            // Return online player names
            return null; // Let Bukkit handle player name completion
        } else if (args.length == 2) {
            // Return subcommands
            return subCommands.stream()
                    .filter(subCmd -> subCmd.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            String action = args[1].toLowerCase();
            
            switch (action) {
                case "removeperm":
                case "listperms":
                    // Return user's permissions
                    org.frizzlenpop.frizzlenRanks.model.World world = plugin.getDataManager().getSelectedWorldObj();
                    User user = world.getUser(args[0]);
                    return user.getPermissions().stream()
                            .filter(perm -> perm.startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList());
                    
                case "removegroup":
                case "listgroups":
                    // Return user's groups
                    world = plugin.getDataManager().getSelectedWorldObj();
                    user = world.getUser(args[0]);
                    return user.getGroups().stream()
                            .filter(group -> group.startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList());
                    
                case "addgroup":
                case "setgroup":
                    // Return available groups
                    world = plugin.getDataManager().getSelectedWorldObj();
                    return world.getGroups().stream()
                            .map(group -> group.getName())
                            .filter(group -> group.startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList());
                    
                case "meta":
                    // Return user's meta keys
                    world = plugin.getDataManager().getSelectedWorldObj();
                    user = world.getUser(args[0]);
                    return user.getMeta().keySet().stream()
                            .filter(key -> key.startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList());
            }
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Handles the addtempgroup command.
     * 
     * @param sender the command sender
     * @param args the command arguments
     * @param username the target username
     * @return true if the command was successful
     */
    private boolean handleAddTempGroup(CommandSender sender, String[] args, String username) {
        if (args.length < 4) {
            sender.sendMessage("§cUsage: /user <username> addtempgroup <group> <duration>");
            sender.sendMessage("§cExample durations: 30s, 10m, 5h, 7d (seconds, minutes, hours, days)");
            return true;
        }
        
        String group = args[2];
        String durationStr = args[3];
        
        org.frizzlenpop.frizzlenRanks.model.World world = plugin.getDataManager().getSelectedWorldObj();
        User user = world.getUser(username);
        
        // Check if the group exists
        if (!world.hasGroup(group)) {
            sender.sendMessage("§cGroup " + group + " does not exist in world " + world.getName());
            return true;
        }
        
        // Parse the duration
        long durationMillis;
        try {
            durationMillis = parseDuration(durationStr);
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cInvalid duration format. Use: 30s, 10m, 5h, 7d (seconds, minutes, hours, days)");
            return true;
        }
        
        // Calculate expiration time
        long expirationTime = System.currentTimeMillis() + durationMillis;
        
        // Add the temporary group
        user.addTemporaryGroup(group, expirationTime);
        
        // Save data if auto-save is enabled
        if (plugin.getConfigManager().autoSave()) {
            plugin.getDataManager().saveWorld(world);
        }
        
        // Format the expiration time for display
        String formattedExpiration = formatExpirationTime(expirationTime);
        
        sender.sendMessage("§aAdded " + username + " to group " + group + " temporarily until " + formattedExpiration);
        
        // Reset the player's permission cache if they are online
        Player targetPlayer = Bukkit.getPlayer(username);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            plugin.resetPlayerPermissionCache(username);
            sender.sendMessage("§aPermissions have been refreshed for online player " + username);
        }
        
        return true;
    }
    
    /**
     * Handles the removetempgroup command.
     * 
     * @param sender the command sender
     * @param args the command arguments
     * @param username the target username
     * @return true if the command was successful
     */
    private boolean handleRemoveTempGroup(CommandSender sender, String[] args, String username) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /user <username> removetempgroup <group>");
            return true;
        }
        
        String group = args[2];
        
        org.frizzlenpop.frizzlenRanks.model.World world = plugin.getDataManager().getSelectedWorldObj();
        User user = world.getUser(username);
        
        // Check if the user has this temporary group
        Map<String, Long> tempGroups = user.getTemporaryGroups();
        if (!tempGroups.containsKey(group)) {
            sender.sendMessage("§c" + username + " does not have temporary group " + group);
            return true;
        }
        
        // Remove the temporary group
        user.removeTemporaryGroup(group);
        
        // Save data if auto-save is enabled
        if (plugin.getConfigManager().autoSave()) {
            plugin.getDataManager().saveWorld(world);
        }
        
        sender.sendMessage("§aRemoved " + username + " from temporary group " + group);
        
        // Reset the player's permission cache if they are online
        Player targetPlayer = Bukkit.getPlayer(username);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            plugin.resetPlayerPermissionCache(username);
            sender.sendMessage("§aPermissions have been refreshed for online player " + username);
        }
        
        return true;
    }
    
    /**
     * Handles the listtempgroups command.
     * 
     * @param sender the command sender
     * @param username the target username
     * @return true if the command was successful
     */
    private boolean handleListTempGroups(CommandSender sender, String username) {
        org.frizzlenpop.frizzlenRanks.model.World world = plugin.getDataManager().getSelectedWorldObj();
        User user = world.getUser(username);
        
        Map<String, Long> tempGroups = user.getTemporaryGroups();
        
        if (tempGroups.isEmpty()) {
            sender.sendMessage("§e" + username + " does not have any temporary groups in world " + world.getName());
            return true;
        }
        
        sender.sendMessage("§eTemporary groups for " + username + " in world " + world.getName() + ":");
        
        for (Map.Entry<String, Long> entry : tempGroups.entrySet()) {
            String groupName = entry.getKey();
            long expirationTime = entry.getValue();
            
            String formattedExpiration = formatExpirationTime(expirationTime);
            String timeLeft = formatTimeLeft(expirationTime);
            
            sender.sendMessage("§f- " + groupName + " §7(expires: " + formattedExpiration + ", " + timeLeft + " left)");
        }
        
        return true;
    }
    
    /**
     * Handles the addtempperm command.
     * 
     * @param sender the command sender
     * @param args the command arguments
     * @param username the target username
     * @return true if the command was successful
     */
    private boolean handleAddTempPermission(CommandSender sender, String[] args, String username) {
        if (args.length < 4) {
            sender.sendMessage("§cUsage: /user <username> addtempperm <permission> <duration>");
            sender.sendMessage("§cExample durations: 30s, 10m, 5h, 7d (seconds, minutes, hours, days)");
            return true;
        }
        
        String permission = args[2];
        String durationStr = args[3];
        
        org.frizzlenpop.frizzlenRanks.model.World world = plugin.getDataManager().getSelectedWorldObj();
        User user = world.getUser(username);
        
        // Parse the duration
        long durationMillis;
        try {
            durationMillis = parseDuration(durationStr);
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cInvalid duration format. Use: 30s, 10m, 5h, 7d (seconds, minutes, hours, days)");
            return true;
        }
        
        // Calculate expiration time
        long expirationTime = System.currentTimeMillis() + durationMillis;
        
        // Add the temporary permission
        user.addTemporaryPermission(permission, expirationTime);
        
        // Save data if auto-save is enabled
        if (plugin.getConfigManager().autoSave()) {
            plugin.getDataManager().saveWorld(world);
        }
        
        // Format the expiration time for display
        String formattedExpiration = formatExpirationTime(expirationTime);
        
        sender.sendMessage("§aAdded temporary permission " + permission + " to " + username + " until " + formattedExpiration);
        
        // Reset the player's permission cache if they are online
        Player targetPlayer = Bukkit.getPlayer(username);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            plugin.resetPlayerPermissionCache(username);
            sender.sendMessage("§aPermissions have been refreshed for online player " + username);
        }
        
        return true;
    }
    
    /**
     * Handles the removetempperm command.
     * 
     * @param sender the command sender
     * @param args the command arguments
     * @param username the target username
     * @return true if the command was successful
     */
    private boolean handleRemoveTempPermission(CommandSender sender, String[] args, String username) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /user <username> removetempperm <permission>");
            return true;
        }
        
        String permission = args[2];
        
        org.frizzlenpop.frizzlenRanks.model.World world = plugin.getDataManager().getSelectedWorldObj();
        User user = world.getUser(username);
        
        // Check if the user has this temporary permission
        Map<String, Long> tempPermissions = user.getTemporaryPermissions();
        if (!tempPermissions.containsKey(permission)) {
            sender.sendMessage("§c" + username + " does not have temporary permission " + permission);
            return true;
        }
        
        // Remove the temporary permission
        user.removeTemporaryPermission(permission);
        
        // Save data if auto-save is enabled
        if (plugin.getConfigManager().autoSave()) {
            plugin.getDataManager().saveWorld(world);
        }
        
        sender.sendMessage("§aRemoved temporary permission " + permission + " from " + username);
        
        // Reset the player's permission cache if they are online
        Player targetPlayer = Bukkit.getPlayer(username);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            plugin.resetPlayerPermissionCache(username);
            sender.sendMessage("§aPermissions have been refreshed for online player " + username);
        }
        
        return true;
    }
    
    /**
     * Handles the listtempperm command.
     * 
     * @param sender the command sender
     * @param username the target username
     * @return true if the command was successful
     */
    private boolean handleListTempPermissions(CommandSender sender, String username) {
        org.frizzlenpop.frizzlenRanks.model.World world = plugin.getDataManager().getSelectedWorldObj();
        User user = world.getUser(username);
        
        Map<String, Long> tempPermissions = user.getTemporaryPermissions();
        
        if (tempPermissions.isEmpty()) {
            sender.sendMessage("§e" + username + " does not have any temporary permissions in world " + world.getName());
            return true;
        }
        
        sender.sendMessage("§eTemporary permissions for " + username + " in world " + world.getName() + ":");
        
        for (Map.Entry<String, Long> entry : tempPermissions.entrySet()) {
            String permName = entry.getKey();
            long expirationTime = entry.getValue();
            
            String formattedExpiration = formatExpirationTime(expirationTime);
            String timeLeft = formatTimeLeft(expirationTime);
            
            sender.sendMessage("§f- " + permName + " §7(expires: " + formattedExpiration + ", " + timeLeft + " left)");
        }
        
        return true;
    }
    
    /**
     * Parses a duration string into milliseconds.
     * 
     * @param duration the duration string (e.g., "30s", "5m", "2h", "7d")
     * @return the duration in milliseconds
     * @throws IllegalArgumentException if the format is invalid
     */
    private long parseDuration(String duration) throws IllegalArgumentException {
        if (duration == null || duration.isEmpty()) {
            throw new IllegalArgumentException("Duration cannot be empty");
        }
        
        // Get the last character (unit)
        char unit = duration.charAt(duration.length() - 1);
        
        // Get the number part
        String numberPart = duration.substring(0, duration.length() - 1);
        long value;
        try {
            value = Long.parseLong(numberPart);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format: " + numberPart);
        }
        
        // Convert to milliseconds based on the unit
        switch (unit) {
            case 's': // seconds
                return value * 1000;
            case 'm': // minutes
                return value * 60 * 1000;
            case 'h': // hours
                return value * 60 * 60 * 1000;
            case 'd': // days
                return value * 24 * 60 * 60 * 1000;
            default:
                throw new IllegalArgumentException("Invalid duration unit: " + unit);
        }
    }
    
    /**
     * Formats an expiration timestamp for display.
     * 
     * @param expirationTime the expiration timestamp in milliseconds
     * @return a formatted date/time string
     */
    private String formatExpirationTime(long expirationTime) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new java.util.Date(expirationTime));
    }
    
    /**
     * Formats the time left until expiration.
     * 
     * @param expirationTime the expiration timestamp in milliseconds
     * @return a formatted "time left" string
     */
    private String formatTimeLeft(long expirationTime) {
        long currentTime = System.currentTimeMillis();
        long timeLeftMillis = expirationTime - currentTime;
        
        if (timeLeftMillis <= 0) {
            return "expired";
        }
        
        long seconds = timeLeftMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        StringBuilder sb = new StringBuilder();
        
        if (days > 0) {
            sb.append(days).append("d ");
            hours = hours % 24;
        }
        
        if (hours > 0 || days > 0) {
            sb.append(hours).append("h ");
            minutes = minutes % 60;
        }
        
        if (minutes > 0 || hours > 0 || days > 0) {
            sb.append(minutes).append("m ");
            seconds = seconds % 60;
        }
        
        sb.append(seconds).append("s");
        
        return sb.toString();
    }
    
    /**
     * Handles the setgroup command.
     *
     * @param sender the command sender
     * @param args the command arguments
     * @param username the target username
     * @return true if the command was successful
     */
    private boolean handleSetGroup(CommandSender sender, String[] args, String username) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /user " + username + " setgroup <group>");
            return true;
        }
        
        String groupName = args[2];
        
        org.frizzlenpop.frizzlenRanks.model.World world = plugin.getDataManager().getSelectedWorldObj();
        User user = world.getUser(username);
        
        // Check if the group exists
        if (!world.hasGroup(groupName)) {
            sender.sendMessage(ChatColor.RED + "Group " + groupName + " does not exist in world " + world.getName());
            return true;
        }
        
        user.clearGroups();
        user.addGroup(groupName);
        
        // Save data if auto-save is enabled
        if (plugin.getConfigManager().autoSave()) {
            plugin.getDataManager().saveWorld(world);
        }
        
        sender.sendMessage(ChatColor.GREEN + "Set user " + username + " to group " + groupName);
        
        // Reset the player's permission cache if they are online
        Player targetPlayer = Bukkit.getPlayer(username);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            plugin.resetPlayerPermissionCache(username);
            sender.sendMessage(ChatColor.GREEN + "Permissions have been refreshed for online player " + username);
        }
        
        return true;
    }
} 