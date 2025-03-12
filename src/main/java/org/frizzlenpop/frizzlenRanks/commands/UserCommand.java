package org.frizzlenpop.frizzlenRanks.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.frizzlenpop.frizzlenRanks.FrizzlenRanks;
import org.frizzlenpop.frizzlenRanks.listeners.PlayerListener;
import org.frizzlenpop.frizzlenRanks.model.User;
import org.frizzlenpop.frizzlenRanks.model.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the /user command.
 */
public class UserCommand implements CommandExecutor, TabCompleter {
    private final FrizzlenRanks plugin;
    private final List<String> subCommands = Arrays.asList(
            "addperm", "removeperm", "listperms", "addgroup", "removegroup", "setgroup", "listgroups", "meta", "info", "world"
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
        World world = plugin.getDataManager().getSelectedWorldObj();
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
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /user " + userName + " setgroup <group>");
                    return true;
                }
                
                groupName = args[2];
                user.clearGroups();
                user.addGroup(groupName);
                sender.sendMessage(ChatColor.GREEN + "Set user " + userName + " to group " + groupName);
                break;
                
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
        org.bukkit.entity.Player player = Bukkit.getPlayer(playerName);
        if (player != null && player.isOnline()) {
            // Create a new PlayerListener instance and use the non-static method
            PlayerListener playerListener = new PlayerListener(plugin);
            playerListener.updatePlayerTabDisplay(player);
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
                    World world = plugin.getDataManager().getSelectedWorldObj();
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
} 