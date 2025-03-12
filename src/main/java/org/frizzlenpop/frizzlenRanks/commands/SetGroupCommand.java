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
import org.frizzlenpop.frizzlenRanks.model.Group;
import org.frizzlenpop.frizzlenRanks.model.User;
import org.frizzlenpop.frizzlenRanks.model.World;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the /setgroup command.
 */
public class SetGroupCommand implements CommandExecutor, TabCompleter {
    private final FrizzlenRanks plugin;
    
    /**
     * Creates a new SetGroupCommand.
     * 
     * @param plugin the plugin instance
     */
    public SetGroupCommand(FrizzlenRanks plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("frizzlenranks.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /setgroup <username> <groupname>");
            return true;
        }
        
        String userName = args[0];
        String groupName = args[1];
        
        World world = plugin.getDataManager().getSelectedWorldObj();
        User user = world.getUser(userName);
        
        // Check if the group exists
        if (!world.hasGroup(groupName)) {
            sender.sendMessage(ChatColor.RED + "Group '" + groupName + "' does not exist.");
            return true;
        }
        
        // Clear existing groups and set the new one
        user.clearGroups();
        user.addGroup(groupName);
        
        sender.sendMessage(ChatColor.GREEN + "Set " + userName + " to group " + groupName);
        notifyPlayer(userName, "You have been set to group " + groupName);
        
        // Save if auto-save is enabled
        if (plugin.getConfigManager().autoSave()) {
            plugin.getDataManager().saveWorld(world);
            
            // Sync user across worlds if global users is enabled
            plugin.getDataManager().syncUserAcrossWorlds(world, userName);
        }
        
        // Always save and sync for setgroup command regardless of settings
        // This ensures rank changes are always properly applied
        plugin.getDataManager().saveWorld(world);
        plugin.getDataManager().syncUserAcrossWorlds(world, userName);
        
        // Log for debugging
        plugin.getLogger().info("Set user " + userName + " to group " + groupName + " (world: " + world.getName() + ")");
        
        // Update player tab display if they're online
        Player player = Bukkit.getPlayer(userName);
        if (player != null && player.isOnline()) {
            // Create a PlayerListener instance to update the tab display
            PlayerListener playerListener = new PlayerListener(plugin);
            playerListener.updatePlayerTabDisplay(player);
            
            // Reset Vault's permission cache for this player
            plugin.resetPlayerPermissionCache(userName);
            
            sender.sendMessage(ChatColor.GREEN + "Updated player tab display with new rank.");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Note: Player is offline. Changes will apply when they reconnect.");
        }
        
        return true;
    }
    
    /**
     * Notifies a player of their group change.
     * 
     * @param playerName the name of the player
     * @param message the message to send
     */
    private void notifyPlayer(String playerName, String message) {
        Player player = Bukkit.getPlayer(playerName);
        if (player != null && player.isOnline()) {
            player.sendMessage(ChatColor.GREEN + message);
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
            // Return available groups
            World world = plugin.getDataManager().getSelectedWorldObj();
            return world.getGroups().stream()
                    .map(Group::getName)
                    .filter(group -> group.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
} 