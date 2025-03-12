package org.frizzlenpop.frizzlenRanks.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenRanks.FrizzlenRanks;
import org.frizzlenpop.frizzlenRanks.model.Track;
import org.frizzlenpop.frizzlenRanks.model.User;
import org.frizzlenpop.frizzlenRanks.model.World;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the /demote command.
 */
public class DemoteCommand implements CommandExecutor, TabCompleter {
    private final FrizzlenRanks plugin;
    
    /**
     * Creates a new DemoteCommand.
     * 
     * @param plugin the plugin instance
     */
    public DemoteCommand(FrizzlenRanks plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("frizzlenranks.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /demote <username> [track]");
            return true;
        }
        
        String userName = args[0];
        World world = plugin.getDataManager().getSelectedWorldObj();
        User user = world.getUser(userName);
        
        // Determine which track to use
        String trackName = args.length > 1 ? args[1] : "staff";
        Track track = plugin.getDataManager().getTrack(trackName);
        
        if (track == null) {
            sender.sendMessage(ChatColor.RED + "Track '" + trackName + "' does not exist.");
            return true;
        }
        
        // Check if the user has permission to use this track
        if (!sender.hasPermission("frizzlenranks.admin") && !sender.hasPermission("tracks." + trackName)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use the track '" + trackName + "'.");
            return true;
        }
        
        // Find the user's current group in the track
        String currentGroup = null;
        for (String group : user.getGroups()) {
            if (track.containsGroup(group)) {
                currentGroup = group;
                break;
            }
        }
        
        if (currentGroup == null) {
            sender.sendMessage(ChatColor.RED + userName + " is not in any group in track '" + trackName + "'.");
            return true;
        }
        
        // Check if the user is already at the lowest group
        if (track.isLowestGroup(currentGroup)) {
            sender.sendMessage(ChatColor.RED + userName + " is already at the lowest group in track '" + trackName + "'.");
            return true;
        }
        
        // Get the previous group
        String previousGroup = track.getPreviousGroup(currentGroup);
        
        if (previousGroup == null) {
            sender.sendMessage(ChatColor.RED + "Could not determine the previous group for " + userName + " in track '" + trackName + "'.");
            return true;
        }
        
        // Apply the demotion based on the track type
        applyDemotion(user, currentGroup, previousGroup);
        
        sender.sendMessage(ChatColor.GREEN + "Demoted " + userName + " from " + currentGroup + " to " + previousGroup);
        notifyPlayer(userName, "You have been demoted from " + currentGroup + " to " + previousGroup);
        
        // Save if auto-save is enabled
        if (plugin.getConfigManager().autoSave()) {
            plugin.getDataManager().saveWorld(world);
        }
        
        return true;
    }
    
    /**
     * Applies a demotion based on the track type.
     * 
     * @param user the user to demote
     * @param currentGroup the user's current group
     * @param newGroup the group to demote to
     */
    private void applyDemotion(User user, String currentGroup, String newGroup) {
        String trackType = plugin.getConfigManager().getTrackType();
        
        switch (trackType.toLowerCase()) {
            case "single":
                // Remove all groups and add the new one
                user.clearGroups();
                user.addGroup(newGroup);
                break;
                
            case "multi":
                // Remove the current group and add the new one
                user.removeGroup(currentGroup);
                user.addGroup(newGroup);
                break;
                
            case "replace":
                // Replace the current group with the new one
                user.removeGroup(currentGroup);
                user.addGroup(newGroup);
                break;
                
            default:
                // Default to single
                user.clearGroups();
                user.addGroup(newGroup);
                break;
        }
    }
    
    /**
     * Notifies a player of their demotion.
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
            // Return available tracks
            return plugin.getDataManager().getTracks().stream()
                    .map(Track::getName)
                    .filter(track -> track.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
} 